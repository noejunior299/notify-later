package ao.techify.notifylater.core.notifications

import android.app.PendingIntent
import java.util.concurrent.ConcurrentHashMap

/**
 * In-memory store of the source app's original tap action (contentIntent),
 * keyed by captured notification id.
 *
 * A PendingIntent cannot be persisted, so after process death the renderer
 * falls back to the source app's launcher intent.
 */
object OriginalIntents {
    private const val MAX_ENTRIES = 500
    private val intents = ConcurrentHashMap<String, PendingIntent>()

    fun put(id: String, intent: PendingIntent?) {
        if (intent == null) return
        if (intents.size >= MAX_ENTRIES) intents.keys.firstOrNull()?.let { intents.remove(it) }
        intents[id] = intent
    }

    fun take(id: String): PendingIntent? = intents.remove(id)
}
