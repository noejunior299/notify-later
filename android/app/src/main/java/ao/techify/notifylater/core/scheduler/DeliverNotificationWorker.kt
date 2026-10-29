package ao.techify.notifylater.core.scheduler

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import ao.techify.notifylater.NotifyLaterApplicationHolder
import ao.techify.notifylater.core.notifications.CaptureLogger
import ao.techify.notifylater.core.notifications.NotificationRenderer
import ao.techify.notifylater.domain.model.NotificationStatus

/**
 * Delivers one held notification after its scheduled delay.
 *
 * Safety: the original notification is hidden immediately at capture and the
 * item is persisted/scheduled right after; if that tail fails, the listener
 * restores the original. If re-posting is impossible (for example
 * POST_NOTIFICATIONS was revoked), the item stays pending and WorkManager
 * retries instead of silently dropping it.
 */
class DeliverNotificationWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val id = inputData.getString(KEY_NOTIFICATION_ID) ?: return Result.failure()
        val app = applicationContext as? NotifyLaterApplicationHolder ?: return Result.failure()
        val repository = app.notificationRepository

        val notification = repository.findById(id) ?: return Result.success()
        if (notification.status != NotificationStatus.PENDING) return Result.success()

        val dueAt = notification.dueAt
        if (dueAt != null && System.currentTimeMillis() < dueAt) {
            // Should be rare (work was enqueued with the remaining delay).
            return Result.retry()
        }

        val renderer = NotificationRenderer(applicationContext)
        if (!renderer.canPost()) {
            CaptureLogger.w(TAG, "delivery delayed: POST_NOTIFICATIONS is missing")
            return Result.retry()
        }

        if (!renderer.render(notification)) {
            CaptureLogger.w(TAG, "delivery failed; keeping notification pending")
            return Result.retry()
        }

        repository.update(
            notification.copy(
                status = NotificationStatus.DELIVERED,
                deliveredAt = System.currentTimeMillis()
            )
        )
        CaptureLogger.d(TAG, "delivered notification id=$id")
        return Result.success()
    }

    companion object {
        const val KEY_NOTIFICATION_ID = "notification_id"
        private const val TAG = "NotifyLaterWorker"
    }
}
