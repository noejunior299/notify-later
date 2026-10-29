package ao.techify.notifylater.core.notifications

import ao.techify.notifylater.NotifyLaterApplicationHolder
import ao.techify.notifylater.core.settings.AppDelayRules
import ao.techify.notifylater.domain.model.NotificationStatus
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * Observes notifications from other apps and holds them for later delivery.
 *
 * Pipeline:
 * 1. Capture minimal, sanitized metadata.
 * 2. Resolve the delay: per-app rule first, global setting as fallback.
 * 3. Persist the notification with a due time (capture time + resolved delay).
 * 4. Schedule a WorkManager job for that due time.
 * 5. Only then cancel the original system notification.
 *
 * Safety:
 * - If POST_NOTIFICATIONS is missing, the original is left untouched.
 * - If persistence or scheduling fails, the original is left untouched.
 * - Calls, alarms, ongoing notifications, and group summaries are ignored.
 * - The listener's own re-published notifications are filtered out.
 */
class NotifyLaterNotificationListener : NotificationListenerService() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    // Lazy: Context (and packageName) are only available after attachment.
    private val session by lazy { NotificationSessionFilter(applicationContext.packageName) }

    override fun onListenerConnected() {
        super.onListenerConnected()
        // Snapshot of currently active notifications. These keys are suppressed
        // for the whole session so pre-existing notifications are never captured.
        val activeKeys: List<String> = try {
            activeNotifications?.mapNotNull { it.key }.orEmpty()
        } catch (t: Throwable) {
            CaptureLogger.w(TAG, "failed to read active notifications", t)
            emptyList()
        }
        session.onConnected(activeKeys)
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
            when (session.decide(sbn.packageName, sbn.key)) {
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
                applicationLabel = resolveApplicationLabel(sbn.packageName),
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
            val key = sbn.key
            scope.launch {
                runCatching {
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
                    val stored = app.notificationRepository.save(captured.copy(dueAt = dueAt))
                    app.notificationScheduler.schedule(stored)

                    runCatching { cancelNotification(key) }
                        .onFailure { error ->
                            // Avoid a duplicate later if the system refused to
                            // cancel the original. Keep the item out of the queue.
                            app.notificationScheduler.cancel(stored.id)
                            app.notificationRepository.update(
                                stored.copy(status = NotificationStatus.DISMISSED)
                            )
                            throw error
                        }

                    CaptureLogger.d(
                        TAG,
                        "held notification id=${stored.id} dueAt=${stored.dueAt} " +
                            "package=${raw.sourcePackage}"
                    )
                }.onFailure {
                    CaptureLogger.w(TAG, "capture failed; original notification remains", it)
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
