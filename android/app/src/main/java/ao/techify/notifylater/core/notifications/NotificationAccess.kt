package ao.techify.notifylater.core.notifications

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.core.app.NotificationManagerCompat

/**
 * Determines whether Notify Later holds Notification Listener access.
 *
 * This is NOT a runtime permission: Android grants it only after the user
 * enables the app in the dedicated system settings screen
 * (Settings > Notifications > Device & app notifications / Notification access).
 * We can query the state but never request it via a dialog.
 */
object NotificationAccess {

    private const val SETTING_ENABLED_LISTENERS = "enabled_notification_listeners"

    fun listenerComponent(context: Context): ComponentName =
        ComponentName(context, NotifyLaterNotificationListener::class.java)

    /** True when this app's listener component is enabled by the system. */
    fun isEnabled(context: Context): Boolean =
        try {
            val component = listenerComponent(context)
            NotificationManagerCompat.getEnabledListenerPackages(context)
                .contains(component.packageName) && isComponentInSetting(context, component)
        } catch (_: Exception) {
            false
        }

    private fun isComponentInSetting(context: Context, component: ComponentName): Boolean {
        return try {
            val flat = Settings.Secure.getString(
                context.contentResolver,
                SETTING_ENABLED_LISTENERS
            ) ?: return false
            flat.split(":").any { entry ->
                ComponentName.unflattenFromString(entry) == component
            }
        } catch (_: Exception) {
            false
        }
    }

    /** Opens the Android system screen where the user grants listener access. */
    fun settingsIntent(): Intent =
        Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
}
