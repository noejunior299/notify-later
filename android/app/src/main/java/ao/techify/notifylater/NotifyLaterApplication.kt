package ao.techify.notifylater

import android.app.Application
import android.content.pm.ApplicationInfo
import ao.techify.notifylater.core.notifications.CaptureLogger
import ao.techify.notifylater.data.repository.NotificationRepositoryImpl
import ao.techify.notifylater.domain.repository.NotificationRepository

/**
 * Application entry point. Holds lightweight singletons for the Phase 1 spike.
 * No DI framework by design; will be revisited when justified.
 */
class NotifyLaterApplication : Application(), NotifyLaterApplicationHolder {

    override val notificationRepository: NotificationRepository by lazy {
        NotificationRepositoryImpl()
    }

    override fun onCreate() {
        super.onCreate()
        CaptureLogger.enabled =
            (applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0
    }
}

/** Accessor interface so components can reach shared singletons without DI. */
interface NotifyLaterApplicationHolder {
    val notificationRepository: NotificationRepository
}
