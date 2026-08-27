package ao.techify.notifylater.core.settings

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

interface SettingsRepository {
    val settings: Flow<AppSettings>
    suspend fun setOnboardingCompleted(completed: Boolean)
    suspend fun setLanguage(language: String)
}

class InMemorySettingsRepository : SettingsRepository {
    private val _settings = MutableStateFlow(AppSettings())
    override val settings: Flow<AppSettings> = _settings

    override suspend fun setOnboardingCompleted(completed: Boolean) {
        _settings.value = _settings.value.copy(onboardingCompleted = completed)
    }

    override suspend fun setLanguage(language: String) {
        _settings.value = _settings.value.copy(language = language)
    }
}
