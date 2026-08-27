package ao.techify.notifylater.data.model

/**
 * Data-layer placeholder mirroring the domain notification for future mappers.
 * Room entities will replace this in Phase 2.
 */
data class NotificationEntity(
    val id: String,
    val sourcePackage: String,
    val sourceApplicationName: String?,
    val title: String?,
    val text: String?,
    val timestamp: Long,
    val originalNotificationKey: String,
    val category: String?,
    val priority: Int
)
