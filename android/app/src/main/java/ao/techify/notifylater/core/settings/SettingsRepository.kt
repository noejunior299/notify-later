package ao.techify.notifylater.core.settings

import android.content.Context
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

interface SettingsRepository {
    val settings: Flow<AppSettings>
    val current: AppSettings
    suspend fun setOnboardingCompleted(completed: Boolean)
    suspend fun setLanguage(language: String)
    suspend fun setDeliveryDelayMillis(delayMillis: Long)
}

class InMemorySettingsRepository : SettingsRepository {
    private val _settings = MutableStateFlow(AppSettings())
    override val settings: Flow<AppSettings> = _settings
    override val current: AppSettings get() = _settings.value

    override suspend fun setOnboardingCompleted(completed: Boolean) {
        _settings.value = _settings.value.copy(onboardingCompleted = completed)
    }

    override suspend fun setLanguage(language: String) {
        _settings.value = _settings.value.copy(language = language)
    }

    override suspend fun setDeliveryDelayMillis(delayMillis: Long) {
        _settings.value = _settings.value.copy(
            deliveryDelayMillis = AppSettings.normalizeDelay(delayMillis)
        )
    }
}

/**
 * Local-only persisted settings. Keeps onboarding, language, and the global
 * delivery delay across process restarts.
 */
class PersistentSettingsRepository(context: Context) : SettingsRepository {

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val _settings = MutableStateFlow(load())
    override val settings: Flow<AppSettings> = _settings.asStateFlow()
    override val current: AppSettings get() = _settings.value

    override suspend fun setOnboardingCompleted(completed: Boolean) {
        update { it.copy(onboardingCompleted = completed) }
    }

    override suspend fun setLanguage(language: String) {
        update { it.copy(language = language) }
    }

    override suspend fun setDeliveryDelayMillis(delayMillis: Long) {
        update { it.copy(deliveryDelayMillis = AppSettings.normalizeDelay(delayMillis)) }
    }

    private fun update(transform: (AppSettings) -> AppSettings) {
        val next = transform(_settings.value)
        prefs.edit()
            .putBoolean(KEY_ONBOARDING_COMPLETED, next.onboardingCompleted)
            .putString(KEY_LANGUAGE, next.language)
            .putLong(KEY_DELIVERY_DELAY, next.deliveryDelayMillis)
            .apply()
        _settings.value = next
    }

    private fun load(): AppSettings {
        val delay = prefs.getLong(KEY_DELIVERY_DELAY, AppSettings.DEFAULT_DELIVERY_DELAY_MILLIS)
        return AppSettings(
            onboardingCompleted = prefs.getBoolean(KEY_ONBOARDING_COMPLETED, false),
            language = prefs.getString(KEY_LANGUAGE, "en") ?: "en",
            deliveryDelayMillis = if (delay in AppSettings.MIN_DELIVERY_DELAY_MILLIS..
                AppSettings.MAX_DELIVERY_DELAY_MILLIS
            ) {
                delay
            } else {
                AppSettings.DEFAULT_DELIVERY_DELAY_MILLIS
            }
        )
    }

    companion object {
        private const val PREFS_NAME = "notify_later_settings"
        private const val KEY_ONBOARDING_COMPLETED = "onboarding_completed"
        private const val KEY_LANGUAGE = "language"
        private const val KEY_DELIVERY_DELAY = "delivery_delay_millis"
    }
}
