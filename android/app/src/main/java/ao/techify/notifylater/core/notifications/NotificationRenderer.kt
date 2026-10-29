package ao.techify.notifylater.core.notifications

import android.app.NotificationChannel
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import ao.techify.notifylater.MainActivity
import ao.techify.notifylater.core.permissions.PermissionHelper
import ao.techify.notifylater.R
import ao.techify.notifylater.domain.model.Notification as CapturedNotification

/**
 * Renders a captured notification as a Notify Later notification.
 *
 * Spike limitations (documented in docs/architecture/notification-pipeline.md):
 * - does not reproduce actions, inline replies, styles, big pictures, progress;
 * - single channel; no per-app channels yet;
 * - small icon is the app icon (per-source icons come later).
 *
 * Fail-safe contract: if rendering is not possible (no POST_NOTIFICATIONS on
 * Android 13+, or any error), nothing is posted AND the original system
 * notification was never cancelled by us, so no data is lost.
 */
class NotificationRenderer(private val context: Context) {

    private val notificationManager = NotificationManagerCompat.from(context)

    fun ensureChannel() {
        val channel = NotificationChannel(
            NotificationHelper.CHANNEL_ID_DEFAULT,
            context.getString(R.string.notification_channel_name),
            android.app.NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = context.getString(R.string.notification_channel_description)
        }
        try {
            notificationManager.createNotificationChannel(channel)
        } catch (e: Exception) {
            CaptureLogger.w(TAG, "channel creation failed", e)
        }
    }

    fun canPost(): Boolean =
        PermissionHelper.hasPostNotificationsPermission(context)

    /**
     * Posts a re-published copy of [captured].
     * Returns true when a local notification was actually posted.
     */
    fun render(captured: CapturedNotification): Boolean {
        if (!canPost()) {
            // Android 13+ without POST_NOTIFICATIONS: do not attempt to post.
            CaptureLogger.d(TAG, "cannot post: missing POST_NOTIFICATIONS")
            return false
        }
        return try {
            ensureChannel()
            val appName = captured.sourceApplicationName ?: captured.sourcePackage
            val displayTitle = captured.title?.takeIf { it.isNotBlank() } ?: appName
            val displayText = captured.text?.takeIf { it.isNotBlank() } ?: appName
            val builder = NotificationCompat.Builder(context, NotificationHelper.CHANNEL_ID_DEFAULT)
                .setSmallIcon(R.drawable.ic_notification)
                .setColor(ContextCompat.getColor(context, R.color.notification_accent))
                .setContentTitle(displayTitle)
                .setContentText(displayText)
                .setSubText(context.getString(R.string.renderer_delivered_by_app, appName))
                .setAutoCancel(true)
                .setContentIntent(contentIntent())
            if (!captured.text.isNullOrBlank()) {
                builder.setStyle(
                    NotificationCompat.BigTextStyle()
                        .bigText(captured.text)
                )
            }
            captured.category?.let { builder.setCategory(it) }

            val id = captured.id.hashCode()
            notificationManager.notify(id, builder.build())
            true
        } catch (e: Exception) {
            // Never crash and never lose data: original was not cancelled.
            CaptureLogger.w(TAG, "render failed; original notification remains", e)
            false
        }
    }

    private fun contentIntent(): PendingIntent? = try {
        PendingIntent.getActivity(
            context,
            0,
            Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    } catch (_: Exception) {
        null
    }

    companion object {
        private const val TAG = "NotifyLaterRenderer"
    }
}
