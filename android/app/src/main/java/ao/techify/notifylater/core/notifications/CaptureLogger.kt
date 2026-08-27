package ao.techify.notifylater.core.notifications

import android.util.Log

/**
 * Minimal logger that only emits when explicitly enabled.
 * Enabled from NotifyLaterApplication when the app is a debug build.
 * Never log notification content in release builds.
 */
object CaptureLogger {
    @Volatile
    var enabled: Boolean = false

    fun d(tag: String, message: String) {
        if (enabled) Log.d(tag, message)
    }

    fun w(tag: String, message: String, throwable: Throwable? = null) {
        if (enabled) Log.w(tag, message, throwable)
    }
}
