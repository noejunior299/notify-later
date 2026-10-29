package ao.techify.notifylater.data.repository

import android.content.Context
import ao.techify.notifylater.domain.model.Notification
import ao.techify.notifylater.domain.model.NotificationStatus
import ao.techify.notifylater.domain.repository.NotificationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.json.JSONArray
import org.json.JSONObject

/**
 * Local-only persisted notification store.
 *
 * Notification content is sensitive, so it stays on the device in the app's
 * private SharedPreferences. There is no network and no backup: the repository
 * is excluded from Android backup rules (see res/xml/backup_rules.xml).
 *
 * Pending notifications are never dropped. Completed history is capped at
 * [MAX_COMPLETED] entries.
 */
class PersistentNotificationRepository(context: Context) : NotificationRepository {

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val mutex = Mutex()
    private val _notifications = MutableStateFlow(load())
    override val notifications: Flow<List<Notification>> = _notifications.asStateFlow()

    override suspend fun save(notification: Notification): Notification = mutex.withLock {
        val current = _notifications.value
        val existingPending = current.lastOrNull {
            it.status == NotificationStatus.PENDING &&
                it.originalNotificationKey == notification.originalNotificationKey
        }
        val stored = if (existingPending != null) {
            notification.copy(
                id = existingPending.id,
                dueAt = existingPending.dueAt ?: notification.dueAt
            )
        } else {
            notification
        }
        val updated = trim(current.filterNot { it.id == stored.id } + stored)
        persist(updated)
        stored
    }

    override suspend fun update(notification: Notification): Unit = mutex.withLock {
        val current = _notifications.value
        if (current.none { it.id == notification.id }) return@withLock
        persist(trim(current.map { if (it.id == notification.id) notification else it }))
    }

    override suspend fun findById(id: String): Notification? = mutex.withLock {
        _notifications.value.firstOrNull { it.id == id }
    }

    override suspend fun clear(): Unit = mutex.withLock {
        persist(emptyList())
    }

    override suspend fun clearCompleted(): Unit = mutex.withLock {
        persist(_notifications.value.filter { it.status == NotificationStatus.PENDING })
    }

    private fun persist(items: List<Notification>) {
        prefs.edit().putString(KEY_NOTIFICATIONS, encode(items)).commit()
        _notifications.value = items
    }

    private fun trim(items: List<Notification>): List<Notification> {
        val pending = items.filter { it.status == NotificationStatus.PENDING }
        val completed = items.filterNot { it.status == NotificationStatus.PENDING }
            .sortedBy { it.timestamp }
            .takeLast(MAX_COMPLETED)
        return (completed + pending).sortedBy { it.timestamp }
    }

    private fun load(): List<Notification> {
        val raw = prefs.getString(KEY_NOTIFICATIONS, null) ?: return emptyList()
        return try {
            val array = JSONArray(raw)
            buildList {
                for (index in 0 until array.length()) {
                    array.optJSONObject(index)?.let { add(decode(it)) }
                }
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    private fun encode(items: List<Notification>): String {
        val array = JSONArray()
        items.forEach { notification ->
            array.put(
                JSONObject().apply {
                    put("id", notification.id)
                    put("sourcePackage", notification.sourcePackage)
                    notification.sourceApplicationName?.let { put("sourceApplicationName", it) }
                    notification.title?.let { put("title", it) }
                    notification.text?.let { put("text", it) }
                    put("timestamp", notification.timestamp)
                    put("originalNotificationKey", notification.originalNotificationKey)
                    notification.category?.let { put("category", it) }
                    put("priority", notification.priority)
                    put("status", notification.status.name)
                    notification.dueAt?.let { put("dueAt", it) }
                    notification.deliveredAt?.let { put("deliveredAt", it) }
                }
            )
        }
        return array.toString()
    }

    private fun decode(json: JSONObject): Notification {
        val status = NotificationStatus.entries.firstOrNull {
            it.name == json.optString("status")
        } ?: NotificationStatus.PENDING
        return Notification(
            id = json.optString("id"),
            sourcePackage = json.optString("sourcePackage"),
            sourceApplicationName = json.nullableString("sourceApplicationName"),
            title = json.nullableString("title"),
            text = json.nullableString("text"),
            timestamp = json.optLong("timestamp"),
            originalNotificationKey = json.optString("originalNotificationKey"),
            category = json.nullableString("category"),
            priority = json.optInt("priority"),
            status = status,
            dueAt = json.nullableLong("dueAt"),
            deliveredAt = json.nullableLong("deliveredAt")
        )
    }

    private fun JSONObject.nullableString(name: String): String? =
        if (!has(name) || isNull(name)) null else optString(name).takeIf { it.isNotEmpty() }

    private fun JSONObject.nullableLong(name: String): Long? =
        if (!has(name) || isNull(name)) null else optLong(name)

    companion object {
        private const val PREFS_NAME = "notify_later_notifications"
        private const val KEY_NOTIFICATIONS = "notifications"
        const val MAX_COMPLETED = 100
    }
}
