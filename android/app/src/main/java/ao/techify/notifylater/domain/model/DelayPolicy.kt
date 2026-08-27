package ao.techify.notifylater.domain.model

/**
 * Describes how long and under what conditions to delay.
 */
sealed interface DelayPolicy {
    data object Immediate : DelayPolicy
    data class Delayed(val delayMillis: Long) : DelayPolicy
}
