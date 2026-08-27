package ao.techify.notifylater.core.notifications

import ao.techify.notifylater.domain.model.Notification

/**
 * Thread-safe session state distinguishing notifications that were already
 * active when [android.service.notification.NotificationListenerService]
 * connected (baseline) from genuinely new notifications.
 *
 * Why: on (re)connection Android may deliver already-active notifications via
 * onNotificationPosted. Re-publishing them produces unwanted copies, so every
 * key present in the connection snapshot is suppressed for the whole session.
 *
 * Concurrency: Android does not guarantee ordering between
 * onListenerConnected() and onNotificationPosted(); all state is guarded by a
 * single lock. Notifications arriving before the connection snapshot exists
 * are ignored (fail-safe: they are most likely part of the initial snapshot).
 *
 * Pure Kotlin: no Android framework dependencies; unit-testable on the JVM.
 */
class NotificationSessionFilter(private val selfPackage: String) {

    sealed interface Decision {
        /** Genuinely new notification: proceed to capture. */
        data object Process : Decision
        /** Posted by Notify Later itself: never captured. */
        data object IgnoreSelf : Decision
        /** Active when the listener connected: never re-published this session. */
        data object IgnoreInitial : Decision
        /** Arrived before the connection snapshot was recorded: ignored, fail-safe. */
        data object IgnoreNotConnected : Decision
    }

    private val lock = Any()
    private val baselineKeys = HashSet<String>()

    @Volatile
    private var connected: Boolean = false

    /**
     * Records the snapshot of currently active notification keys taken at
     * connection time and marks the session connected.
     * Replaces any previous baseline (used again on reconnection).
     */
    fun onConnected(activeNotificationKeys: Collection<String>) {
        synchronized(lock) {
            baselineKeys.clear()
            activeNotificationKeys
                .filter { it.isNotBlank() }
                .forEach { baselineKeys.add(it) }
            connected = true
        }
    }

    /** Clears session state (e.g., service shutdown). */
    fun reset() {
        synchronized(lock) {
            baselineKeys.clear()
            connected = false
        }
    }

    /** Number of baseline keys recorded at connection time. Debug/diagnostics only. */
    fun baselineCount(): Int = synchronized(lock) { baselineKeys.size }

    fun isConnected(): Boolean = synchronized(lock) { connected }

    /**
     * Decides what to do with an incoming notification event.
     * Blank keys intentionally fall through to [Process]; they are rejected
     * later by the existing [NotificationCapture] rules (single validation path).
     */
    fun decide(sourcePackage: String?, notificationKey: String?): Decision {
        // Self filter always wins, independent of connection state.
        if (sourcePackage != null &&
            NotificationCapture.isSelfNotification(sourcePackage, selfPackage)
        ) {
            return Decision.IgnoreSelf
        }
        synchronized(lock) {
            if (!connected) return Decision.IgnoreNotConnected
            if (notificationKey != null && baselineKeys.contains(notificationKey)) {
                return Decision.IgnoreInitial
            }
        }
        return Decision.Process
    }

    companion object {
        /** Convenience wrapper around the shared pure rules. */
        fun isSelfNotification(sourcePackage: String, selfPackage: String): Boolean =
            NotificationCapture.isSelfNotification(sourcePackage, selfPackage)
    }
}
