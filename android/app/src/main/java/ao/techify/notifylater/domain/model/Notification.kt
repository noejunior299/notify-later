package ao.techify.notifylater.domain.model

/**
 * Normalized representation of a captured notification.
 *
 * Captured information is intentionally minimal and conservative:
 * - id: unique id generated at capture time
 * - sourcePackage / sourceApplicationName: where the notification came from
 * - title / text: user-visible content, sanitized and length-limited
 * - timestamp: original posting time
 * - originalNotificationKey: Android key, used to reference the original
 * - category / priority: coarse metadata only
 *
 * See docs/architecture/notification-pipeline.md for what is captured and ignored.
 */
data class Notification(
    val id: String,
    val sourcePackage: String,
    val sourceApplicationName: String?,
    val title: String?,
    val text: String?,
    val timestamp: Long,
    val originalNotificationKey: String,
    val category: String? = null,
    val priority: Int = 0,
    val status: NotificationStatus = NotificationStatus.PENDING
)
