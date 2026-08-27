package ao.techify.notifylater.core.permissions

import android.content.Context
import android.os.Build

/**
 * Permission helpers.
 * Notification access (NotificationListenerService) will be handled here in Phase 1.
 * POST_NOTIFICATIONS is relevant for re-posting held notifications (Phase 2).
 */
object PermissionHelper {
    fun isPostNotificationsRequired(): Boolean =
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU

    fun hasPostNotificationsPermission(context: Context): Boolean {
        if (!isPostNotificationsRequired()) return true
        return androidx.core.content.ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.POST_NOTIFICATIONS
        ) == android.content.pm.PackageManager.PERMISSION_GRANTED
    }
}
