package ao.techify.notifylater

import android.app.Application
import android.content.pm.ApplicationInfo
import ao.techify.notifylater.core.notifications.CaptureLogger
import ao.techify.notifylater.core.scheduler.NotificationScheduler
import ao.techify.notifylater.core.scheduler.WorkManagerNotificationScheduler
import ao.techify.notifylater.core.settings.AppDelayRulesRepository
import ao.techify.notifylater.core.settings.PersistentAppDelayRulesRepository
import ao.techify.notifylater.core.settings.PersistentSettingsRepository
import ao.techify.notifylater.core.settings.SettingsRepository
import ao.techify.notifylater.data.repository.PersistentNotificationRepository
import ao.techify.notifylater.domain.model.NotificationStatus
import ao.techify.notifylater.domain.repository.NotificationRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * Application entry point. Holds lightweight singletons without a DI framework.
 *
 * Notification state is persisted locally so a held notification survives the
 * process being killed between capture and delivery.
 */
class NotifyLaterApplication : Application(), NotifyLaterApplicationHolder {

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override val notificationRepository: NotificationRepository by lazy {
        PersistentNotificationRepository(this)
    }

    override val settingsRepository: SettingsRepository by lazy {
        PersistentSettingsRepository(this)
    }

    override val notificationScheduler: NotificationScheduler by lazy {
        WorkManagerNotificationScheduler(this)
    }

    override val appDelayRulesRepository: AppDelayRulesRepository by lazy {
        PersistentAppDelayRulesRepository(this)
    }

    override fun completeOnboarding() {
        appScope.launch { settingsRepository.setOnboardingCompleted(true) }
    }

    override fun onCreate() {
        super.onCreate()
        CaptureLogger.enabled =
            (applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0

        // WorkManager persists its own queue, but force-stops or OEM cleanup can
        // drop it. Re-enqueue anything still pending using the remaining delay;
        // unique work name + REPLACE prevents duplicate deliveries.
        appScope.launch {
            runCatching {
                notificationRepository.notifications.first()
                    .filter { it.status == NotificationStatus.PENDING && it.dueAt != null }
                    .forEach { notificationScheduler.schedule(it) }
            }.onFailure {
                CaptureLogger.w(TAG, "failed to restore pending deliveries", it)
            }
        }
    }

    private companion object {
        const val TAG = "NotifyLaterApplication"
    }
}

/** Accessor interface so components can reach shared singletons without DI. */
interface NotifyLaterApplicationHolder {
    val notificationRepository: NotificationRepository
    val settingsRepository: SettingsRepository
    val notificationScheduler: NotificationScheduler
    val appDelayRulesRepository: AppDelayRulesRepository

    /** Persists onboarding completion outside the composition lifecycle. */
    fun completeOnboarding()
}
