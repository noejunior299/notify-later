package ao.techify.notifylater.core.notifications

import ao.techify.notifylater.domain.model.Notification

/**
 * Thread-safe session state distinguishing notifications that were already
 * active when [android.service.notification.NotificationListenerService]
 * connected (baseline) from genuinely new notifications.
 *
 * Why: on (re)connection Android may deliver already-active notifications via
 * onNotificationPosted. Re-publishing them produces unwanted copies, so every
 * key present in the connection snapshot is suppressed only while its content
 * matches the snapshot. A changed content signature means an update (for
 * example a new WhatsApp message in the same conversation key) and is captured.
 * Content signatures are hashes, never raw notification text.
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
        /** Active when the listener connected: ignored until it is updated. */
        data object IgnoreInitial : Decision
        /** Arrived before the connection snapshot was recorded: ignored, fail-safe. */
        data object IgnoreNotConnected : Decision
    }

    private val lock = Any()
    private val baselineSignatures = HashMap<String, String>()

    @Volatile
    private var connected: Boolean = false

    /**
     * Records the snapshot of currently active notifications (key to content
     * signature) taken at connection time and marks the session connected.
     * Replaces any previous baseline (used again on reconnection).
     */
    fun onConnected(activeNotifications: Map<String, String>) {
        synchronized(lock) {
            baselineSignatures.clear()
            activeNotifications
                .filterKeys { it.isNotBlank() }
                .forEach { (key, signature) -> baselineSignatures[key] = signature }
            connected = true
        }
    }

    /**
     * Convenience overload for callers that only have keys. Baseline keys are
     * suppressed until [decide] receives a different content signature.
     */
    fun onConnected(activeNotificationKeys: Collection<String>) {
        onConnected(
            activeNotificationKeys
                .filter { it.isNotBlank() }
                .associateWith { "" }
        )
    }

    /** Clears session state (e.g., service shutdown). */
    fun reset() {
        synchronized(lock) {
            baselineSignatures.clear()
            connected = false
        }
    }

    /** Number of baseline entries recorded at connection time. Debug/diagnostics only. */
    fun baselineCount(): Int = synchronized(lock) { baselineSignatures.size }

    fun isConnected(): Boolean = synchronized(lock) { connected }

    /**
     * Decides what to do with an incoming notification event.
     * Blank keys intentionally fall through to [Process]; they are rejected
     * later by the existing [NotificationCapture] rules (single validation path).
     *
     * @param signature content signature of this event. A baseline key is
     *   ignored only while its signature matches the connection snapshot; a
     *   different signature is an update with new content and is processed.
     */
    fun decide(
        sourcePackage: String?,
        notificationKey: String?,
        signature: String? = null
    ): Decision {
        // Self filter always wins, independent of connection state.
        if (sourcePackage != null &&
            NotificationCapture.isSelfNotification(sourcePackage, selfPackage)
        ) {
            return Decision.IgnoreSelf
        }
        synchronized(lock) {
            if (!connected) return Decision.IgnoreNotConnected
            if (notificationKey != null) {
                val baseline = baselineSignatures[notificationKey]
                if (baseline != null) {
                    if (signature == null || signature == baseline) {
                        return Decision.IgnoreInitial
                    }
                    // The key now carries fresh content; future events must not
                    // be mistaken for the snapshot notification again.
                    baselineSignatures.remove(notificationKey)
                }
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
