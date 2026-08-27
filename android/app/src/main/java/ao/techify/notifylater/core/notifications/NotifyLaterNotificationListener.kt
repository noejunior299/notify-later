package ao.techify.notifylater.core.notifications

import ao.techify.notifylater.NotifyLaterApplicationHolder
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Phase 1 spike: observes notifications from other apps, captures minimal
 * metadata, stores it in-memory for the debug screen, and re-publishes a
 * Notify Later copy.
 *
 * SAFETY: this spike NEVER cancels the original notification. The original
 * stays visible until we have reliable reproduction + persistence (Phase 2).
 * If anything fails here, the original notification remains untouched.
 *
 * This class is an adapter/orchestrator only; logic lives in
 * [NotificationCapture] (pure), [NotificationRepository] and
 * [NotificationRenderer].
 */
class NotifyLaterNotificationListener : NotificationListenerService() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    // Lazy: Context (and packageName) are only available after attachment.
    private val session by lazy { NotificationSessionFilter(applicationContext.packageName) }

    override fun onListenerConnected() {
        super.onListenerConnected()
        // Snapshot of currently active notifications. These keys are suppressed
        // for the whole session so pre-existing notifications are never
        // re-published by the Phase 1 spike.
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
            when (session.decide(sbn.packageName, sbn.key)) {
                NotificationSessionFilter.Decision.IgnoreSelf -> {
                    // Never capture ourselves (prevents recursion). No content logged.
                    CaptureLogger.d(TAG, "ignored self notification")
                    return
                }
                NotificationSessionFilter.Decision.IgnoreInitial -> {
                    // Active before the listener connected: never re-published.
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
            val extras = sbn.notification?.extras
            val raw = RawNotification(
                sourcePackage = sbn.packageName,
                applicationLabel = resolveApplicationLabel(sbn.packageName),
                fallbackApplicationLabel = sbn.packageName,
                title = extras?.getCharSequence(android.app.Notification.EXTRA_TITLE),
                text = extras?.getCharSequence(android.app.Notification.EXTRA_TEXT),
                timestamp = sbn.postTime,
                notificationKey = sbn.key ?: "",
                category = sbn.notification?.category,
                priority = sbn.notification?.priority ?: 0
            )
            val captured = NotificationCapture.capture(raw, selfPackage)
                ?: return // filtered or invalid: original untouched

            CaptureLogger.d(
                TAG,
                "captured package=${raw.sourcePackage} key=${raw.notificationKey}"
            )

            scope.launch {
                runCatching { repository().save(captured) }
                    .onFailure { CaptureLogger.w(TAG, "save failed", it) }
                // Re-publication only after capture succeeded; original not cancelled.
                runCatching { renderer().render(captured) }
                    .onFailure { CaptureLogger.w(TAG, "render failed", it) }
            }
        } catch (t: Throwable) {
            // Fail safe: any unexpected error leaves the original notification alone.
            CaptureLogger.w(TAG, "capture failed; original notification remains", t)
        }
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        // Not used in the spike.
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

    private fun repository() = (applicationContext as NotifyLaterApplicationHolder).notificationRepository

    private fun renderer() = NotificationRenderer(applicationContext)

    companion object {
        private const val TAG = "NotifyLaterListener"
    }
}
