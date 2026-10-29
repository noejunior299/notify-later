package ao.techify.notifylater.core.notifications

import ao.techify.notifylater.NotifyLaterApplicationHolder
import ao.techify.notifylater.core.settings.AppDelayRules
import ao.techify.notifylater.domain.model.NotificationStatus
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * Observes notifications from other apps and holds them for later delivery.
 *
 * Pipeline:
 * 1. Capture minimal, sanitized metadata.
 * 2. Resolve the delay: per-app rule first, global setting as fallback.
 * 3. Hide the original from the shade immediately (before disk/WorkManager I/O).
 * 4. Persist the notification with a due time (capture time + resolved delay).
 * 5. Schedule a WorkManager job for that due time.
 * 6. If persistence or scheduling fails after the original was hidden,
 *    re-post it immediately instead of losing it.
 *
 * Safety:
 * - If POST_NOTIFICATIONS is missing, the original is left untouched.
 * - Calls, alarms, ongoing notifications, and group summaries are never queued
 *   (summaries are hidden together with their children).
 * - The listener's own re-published notifications are filtered out.
 */
class NotifyLaterNotificationListener : NotificationListenerService() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    // Lazy: Context (and packageName) are only available after attachment.
    private val session by lazy { NotificationSessionFilter(applicationContext.packageName) }

    override fun onListenerConnected() {
        super.onListenerConnected()
        // Snapshot of currently active notifications. These keys are suppressed
        // only while their content still matches this snapshot; pre-existing
        // notifications are never captured, but later updates to the same key
        // (a new WhatsApp message in the same chat) are.
        val active: Map<String, String> = try {
            activeNotifications
                ?.mapNotNull { active -> active.key?.let { it to signatureOf(active) } }
                ?.toMap()
                .orEmpty()
        } catch (t: Throwable) {
            CaptureLogger.w(TAG, "failed to read active notifications", t)
            emptyMap()
        }
        session.onConnected(active)
        CaptureLogger.d(TAG, "listener connected; baseline notifications=${session.baselineCount()}")
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        if (sbn == null) return
        try {
            val selfPackage = packageName
            val holder = applicationContext as? NotifyLaterApplicationHolder
            if (holder?.settingsRepository?.current?.enabled == false) {
                CaptureLogger.d(TAG, "app disabled; leaving notification untouched")
                return
            }
            when (session.decide(sbn.packageName, sbn.key, signatureOf(sbn))) {
                NotificationSessionFilter.Decision.IgnoreSelf -> {
                    // Never capture ourselves (prevents recursion). No content logged.
                    CaptureLogger.d(TAG, "ignored self notification")
                    return
                }
                NotificationSessionFilter.Decision.IgnoreInitial -> {
                    // Active before the listener connected: never captured.
                    CaptureLogger.d(TAG, "ignored initial active notification")
                    return
                }
                NotificationSessionFilter.Decision.IgnoreNotConnected -> {
                    // Arrived before the baseline snapshot existed; fail safe.
                    CaptureLogger.d(TAG, "ignored notification before listener connected")
                    return
                }
                NotificationSessionFilter.Decision.Process -> Unit
            }

            val notification = sbn.notification
            val extras = notification?.extras
            val raw = RawNotification(
                sourcePackage = sbn.packageName,
                // The app label is resolved after the original is hidden: a
                // PackageManager binder call must not delay cancellation.
                applicationLabel = null,
                fallbackApplicationLabel = sbn.packageName,
                title = extras?.getCharSequence(android.app.Notification.EXTRA_TITLE),
                text = extras?.getCharSequence(android.app.Notification.EXTRA_TEXT),
                timestamp = sbn.postTime,
                notificationKey = sbn.key ?: "",
                category = notification?.category,
                priority = notification?.priority ?: 0,
                isOngoing = sbn.isOngoing,
                isGroupSummary = notification != null &&
                    (notification.flags and android.app.Notification.FLAG_GROUP_SUMMARY) != 0
            )
            if (raw.isGroupSummary) {
                // The summary ("2 new messages") must disappear with its
                // children, otherwise WhatsApp content stays in the shade.
                // Summaries are hidden, never queued.
                if (NotificationCapture.shouldHoldGroupSummary(raw, selfPackage) &&
                    NotificationRenderer(applicationContext).canPost()
                ) {
                    cancelOriginal(raw.notificationKey)
                }
                return
            }

            val captured = NotificationCapture.capture(raw, selfPackage)
                ?: return // filtered or invalid: original untouched

            if (!NotificationRenderer(applicationContext).canPost()) {
                CaptureLogger.w(
                    TAG,
                    "POST_NOTIFICATIONS missing; leaving original notification untouched"
                )
                return
            }

            CaptureLogger.d(
                TAG,
                "captured package=${raw.sourcePackage} key=${raw.notificationKey}"
            )

            val app = applicationContext as NotifyLaterApplicationHolder
            val key = raw.notificationKey

            // Hide the original before touching disk or WorkManager. Any I/O
            // here is a window where the source app's notification is still
            // visible; cancelling first is what makes "held" feel instant.
            val hiddenImmediately = cancelOriginal(key)

            scope.launch {
                var stored: ao.techify.notifylater.domain.model.Notification? = null
                var display: ao.techify.notifylater.domain.model.Notification = captured
                try {
                    val globalDelay =
                        app.settingsRepository.settings.first().deliveryDelayMillis
                    // Per-app rule wins over the global delay (WhatsApp 30 min,
                    // Facebook 1 h, Phone 5 min, …); no rule falls back to global.
                    val delay = AppDelayRules.resolve(
                        rules = app.appDelayRulesRepository.rules.first(),
                        packageName = raw.sourcePackage,
                        globalDelayMillis = globalDelay
                    )
                    val dueAt = System.currentTimeMillis() + delay
                    display = captured.copy(
                        sourceApplicationName = resolveApplicationLabel(raw.sourcePackage)
                            ?: captured.sourceApplicationName
                    )
                    stored = app.notificationRepository.save(display.copy(dueAt = dueAt))
                    app.notificationScheduler.schedule(stored)

                    if (!hiddenImmediately) {
                        // Retry once now that I/O is done; a transient listener
                        // disconnect can make the first cancel a no-op.
                        cancelOriginal(key)
                    }

                    // Some apps re-post the same notification right after it is
                    // dismissed. Check once more so the shade does not keep a
                    // stale copy while the delayed one waits in the queue.
                    delay(350)
                    if (isNotificationActive(key)) {
                        CaptureLogger.d(TAG, "original reappeared; hiding again key=$key")
                        cancelOriginal(key)
                    }

                    CaptureLogger.d(
                        TAG,
                        "held notification id=${stored.id} dueAt=${stored.dueAt} " +
                            "package=${raw.sourcePackage}"
                    )
                } catch (t: Throwable) {
                    CaptureLogger.w(TAG, "capture failed after hiding original", t)
                    // The item must not stay in the queue if its delivery job
                    // was never scheduled, or it would be delivered twice.
                    stored?.let { pending ->
                        runCatching {
                            app.notificationRepository.update(
                                pending.copy(status = NotificationStatus.DISMISSED)
                            )
                        }
                    }
                    if (hiddenImmediately) {
                        val restored = NotificationRenderer(applicationContext).render(display)
                        CaptureLogger.w(
                            TAG,
                            "restored notification after persistence failure restored=$restored",
                            t
                        )
                    }
                }
            }
        } catch (t: Throwable) {
            // Fail safe: any unexpected error leaves the original notification alone.
            CaptureLogger.w(TAG, "capture failed; original notification remains", t)
        }
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        // Delivery and dismissal are tracked by the repository/worker.
    }

    /**
     * A stable, non-reversible signature of the notification's visible content.
     * Used only to tell the connection snapshot apart from later updates; raw
     * text is never stored or logged.
     */
    private fun signatureOf(sbn: StatusBarNotification): String {
        val notification = sbn.notification
        val extras = notification?.extras
        val title = extras
            ?.getCharSequence(android.app.Notification.EXTRA_TITLE)
            ?.toString()
            .orEmpty()
        val text = extras
            ?.getCharSequence(android.app.Notification.EXTRA_TEXT)
            ?.toString()
            .orEmpty()
        return buildString {
            append(notification?.`when` ?: 0L)
            append(':')
            append(sbn.postTime)
            append(':')
            append(title.hashCode())
            append(':')
            append(text.hashCode())
            append(':')
            append(notification?.flags ?: 0)
        }
    }

    /**
     * Hides the source notification from the shade. Runs on the listener
     * callback thread before any disk or WorkManager work, because every
     * millisecond spent elsewhere keeps the original visible.
     *
     * Returns false when the framework throws; the caller retries once after
     * scheduling so a transient listener disconnect does not leak a copy.
     */
    private fun cancelOriginal(key: String): Boolean = try {
        cancelNotification(key)
        CaptureLogger.d(TAG, "original hidden key=$key")
        true
    } catch (t: Throwable) {
        CaptureLogger.w(TAG, "failed to hide original key=$key", t)
        false
    }

    /** Whether the source notification is still present in the shade. */
    private fun isNotificationActive(key: String): Boolean = try {
        activeNotifications?.any { it.key == key } == true
    } catch (t: Throwable) {
        CaptureLogger.w(TAG, "failed to read active notifications", t)
        false
    }

    override fun onDestroy() {
        CaptureLogger.d(TAG, "listener destroyed")
        session.reset()
        super.onDestroy()
    }

    private fun resolveApplicationLabel(pkg: String): String? = try {
        val pm = applicationContext.packageManager
        pm.getApplicationLabel(pm.getApplicationInfo(pkg, 0))?.toString()
    } catch (_: Exception) {
        null
    }

    companion object {
        private const val TAG = "NotifyLaterListener"
    }
}
