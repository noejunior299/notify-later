package ao.techify.notifylater.domain.repository

/**
 * Domain-level settings repository interface if needed in use cases.
 * Core settings already has an interface; this mirrors for domain layer.
 */
interface AppSettingsRepository {
    suspend fun isOnboardingCompleted(): Boolean
    suspend fun setOnboardingCompleted(completed: Boolean)
}
