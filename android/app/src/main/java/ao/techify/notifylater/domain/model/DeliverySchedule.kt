package ao.techify.notifylater.domain.model

/**
 * Describes when held notifications are released.
 */
data class DeliverySchedule(
    val windowStartMillis: Long,
    val windowEndMillis: Long
)
