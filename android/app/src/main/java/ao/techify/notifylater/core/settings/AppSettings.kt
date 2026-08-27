package ao.techify.notifylater.core.settings

/**
 * Settings storage placeholder.
 * Will wrap DataStore / SharedPreferences in later phases.
 * Used for onboarding completion, language preference, etc.
 */
data class AppSettings(
    val onboardingCompleted: Boolean = false,
    val language: String = "en"
)
