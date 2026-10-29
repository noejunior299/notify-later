package ao.techify.notifylater.core.notifications

import ao.techify.notifylater.domain.model.Notification

/**
 * Platform-independent input extracted from an Android notification.
 * The listener maps framework types into this simple structure so that all
 * transformation logic can run and be unit-tested on the JVM.
 */
data class RawNotification(
    val sourcePackage: String,
    val applicationLabel: String? = null,
    val fallbackApplicationLabel: String? = null,
    val title: CharSequence? = null,
    val text: CharSequence? = null,
    val timestamp: Long = 0L,
    val notificationKey: String = "",
    val category: String? = null,
    val priority: Int = 0,
    val isOngoing: Boolean = false,
    val isGroupSummary: Boolean = false
)

/**
 * Pure capture/transformation rules. No Android framework dependencies.
 *
 * Safety contract:
 * - returns null whenever capture should NOT happen (self notifications,
 *   missing key); the original system notification remains untouched.
 * - content is sanitized (trimmed, length limited) before it leaves this class.
 */
object NotificationCapture {

    const val MAX_TITLE_LENGTH = 200
    const val MAX_TEXT_LENGTH = 1000

    /**
     * Calls, alarms, and foreground services must never be removed from the
     * shade: delaying them could hide something urgent or break an active
     * service. They are left untouched.
     */
    private val IGNORED_CATEGORIES = setOf("call", "alarm", "service")

    /** System chrome (SD card, screen lock, USB) must stay in the shade. */
    private val IGNORED_PACKAGES = setOf("android", "com.android.systemui")

    /** Notifications posted by Notify Later itself must never be captured. */
    fun isSelfNotification(sourcePackage: String, selfPackage: String): Boolean =
        sourcePackage == selfPackage

    /**
     * Shared safety filters. Group summaries are allowed through this base
     * check because they must be hidden together with their children; content
     * capture is decided separately by [shouldCapture].
     */
    private fun passesBaseFilters(raw: RawNotification, selfPackage: String): Boolean =
        !isSelfNotification(raw.sourcePackage, selfPackage) &&
            raw.sourcePackage !in IGNORED_PACKAGES &&
            raw.notificationKey.isNotBlank() &&
            raw.timestamp > 0L &&
            !raw.isOngoing &&
            raw.category?.lowercase() !in IGNORED_CATEGORIES

    fun shouldCapture(raw: RawNotification, selfPackage: String): Boolean =
        passesBaseFilters(raw, selfPackage) && !raw.isGroupSummary

    /**
     * Group summaries ("2 new messages") must be hidden from the shade while
     * their children are held, otherwise the source app still exposes content.
     * They are never queued themselves.
     */
    fun shouldHoldGroupSummary(raw: RawNotification, selfPackage: String): Boolean =
        raw.isGroupSummary && passesBaseFilters(raw, selfPackage)

    /** Trims and length-limits text; blank input becomes null. */
    fun sanitizeText(raw: CharSequence?, maxLength: Int): String? {
        val value = raw?.toString()?.trim().orEmpty()
        if (value.isEmpty()) return null
        return if (value.length <= maxLength) value else value.take(maxLength)
    }

    /**
     * Transforms raw input into a domain Notification, or null if the event
     * must not be captured. Never throws.
     */
    fun capture(raw: RawNotification, selfPackage: String): Notification? {
        if (!shouldCapture(raw, selfPackage)) return null
        return try {
            Notification(
                id = "${raw.timestamp}_${raw.notificationKey.hashCode()}",
                sourcePackage = raw.sourcePackage,
                sourceApplicationName = raw.applicationLabel
                    ?.trim()
                    ?.takeIf { it.isNotEmpty() }
                    ?: raw.fallbackApplicationLabel?.trim()?.takeIf { it.isNotEmpty() },
                title = sanitizeText(raw.title, MAX_TITLE_LENGTH),
                text = sanitizeText(raw.text, MAX_TEXT_LENGTH),
                timestamp = raw.timestamp,
                originalNotificationKey = raw.notificationKey,
                category = raw.category?.trim()?.takeIf { it.isNotEmpty() },
                priority = raw.priority
            )
        } catch (_: Exception) {
            // Fail safe: never let a malformed notification break the pipeline.
            null
        }
    }
}
