package ao.techify.notifylater.domain.model

/**
 * Ordered collection of held notifications awaiting delivery.
 */
data class NotificationQueue(
    val items: List<Notification> = emptyList()
)
