package ao.techify.notifylater.core.scheduler

import android.content.Context
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import ao.techify.notifylater.domain.model.Notification
import java.util.concurrent.TimeUnit

/**
 * Schedules delivery of a held notification.
 */
interface NotificationScheduler {
    fun schedule(notification: Notification)
    fun cancel(notificationId: String)
}

/**
 * WorkManager-backed scheduler. One unique work item per notification keeps
 * delivery independent, survives process restarts, and does not need exact
 * alarm permissions.
 */
class WorkManagerNotificationScheduler(context: Context) : NotificationScheduler {

    private val workManager = WorkManager.getInstance(context.applicationContext)

    override fun schedule(notification: Notification) {
        val dueAt = notification.dueAt ?: return
        val delay = (dueAt - System.currentTimeMillis()).coerceAtLeast(0L)
        val request = OneTimeWorkRequestBuilder<DeliverNotificationWorker>()
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .setInputData(
                workDataOf(DeliverNotificationWorker.KEY_NOTIFICATION_ID to notification.id)
            )
            .addTag(TAG)
            .build()

        workManager.enqueueUniqueWork(
            workName(notification.id),
            ExistingWorkPolicy.REPLACE,
            request
        )
    }

    override fun cancel(notificationId: String) {
        workManager.cancelUniqueWork(workName(notificationId))
    }

    private fun workName(notificationId: String) = "$WORK_PREFIX$notificationId"

    companion object {
        private const val TAG = "notify_later_delivery"
        private const val WORK_PREFIX = "notify_later_deliver_"
    }
}
