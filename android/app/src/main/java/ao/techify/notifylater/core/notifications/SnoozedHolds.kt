package ao.techify.notifylater.core.notifications

import android.content.Context

/**
 * Notifications held via the system snooze (NotificationListenerService.snoozeNotification).
 *
 * With snooze the system itself keeps the ORIGINAL notification and re-posts
 * it when the delay ends, so its tap action (deep link), actions and icon are
 * preserved even if Notify Later's process is killed. This store maps the
 * original key to the queued item so the listener can recognise the system
 * re-post and mark the item delivered instead of capturing it again.
 *
 * Persisted in SharedPreferences so it survives process death.
 */
class SnoozedHolds(context: Context) {

    data class Hold(val notificationId: String, val dueAt: Long)

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val released = context.getSharedPreferences(RELEASED_PREFS_NAME, Context.MODE_PRIVATE)

    /**
     * Remembers that [key] was handed back to the user with this content.
     * The system often re-posts the same notification right after (autogroup
     * regrouping, ranking updates); those must not be held again, or the
     * notification would bounce between snooze and shade forever. Only a
     * content change (a new message) is held again.
     */
    @Synchronized
    fun markReleased(key: String, contentSignature: String) {
        released.edit().putString(key, "${System.currentTimeMillis()}|$contentSignature").apply()
        // Keep the store small: drop entries older than a day.
        val cutoff = System.currentTimeMillis() - 24 * 60 * 60_000L
        val stale = released.all.filter { (_, v) ->
            ((v as? String)?.substringBefore('|')?.toLongOrNull() ?: 0L) < cutoff
        }.keys
        if (stale.isNotEmpty()) released.edit().apply { stale.forEach { remove(it) } }.apply()
    }

    /** True when [key] was already released with exactly this content. */
    @Synchronized
    fun isReleasedUnchanged(key: String, contentSignature: String): Boolean {
        val raw = released.getString(key, null) ?: return false
        if (raw.substringAfter('|') == contentSignature) return true
        released.edit().remove(key).apply()
        return false
    }

    @Synchronized
    fun put(key: String, notificationId: String, dueAt: Long) {
        prefs.edit().putString(key, "$dueAt|$notificationId").apply()
    }

    @Synchronized
    fun get(key: String): Hold? {
        val raw = prefs.getString(key, null) ?: return null
        val sep = raw.indexOf('|')
        if (sep <= 0) return null
        val dueAt = raw.substring(0, sep).toLongOrNull() ?: return null
        return Hold(raw.substring(sep + 1), dueAt)
    }

    @Synchronized
    fun remove(key: String) {
        prefs.edit().remove(key).apply()
    }

    /** Original key held for [notificationId], if any. */
    @Synchronized
    fun keyFor(notificationId: String): String? =
        prefs.all.entries.firstOrNull { (_, v) ->
            (v as? String)?.substringAfter('|') == notificationId
        }?.key

    @Synchronized
    fun all(): Map<String, Hold> = prefs.all.keys.mapNotNull { k -> get(k)?.let { k to it } }.toMap()

    companion object {
        private const val PREFS_NAME = "notify_later_snoozed"
        private const val RELEASED_PREFS_NAME = "notify_later_released"

        /** A re-post this close to (or after) dueAt is the system ending the snooze. */
        const val REPOST_TOLERANCE_MILLIS = 5_000L

        /**
         * The fallback worker runs this long after dueAt; if the system re-post
         * was not observed by then, a copy is rendered instead.
         */
        const val FALLBACK_GRACE_MILLIS = 45_000L
    }
}
