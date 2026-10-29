package ao.techify.notifylater.data.repository

import ao.techify.notifylater.domain.model.Notification
import ao.techify.notifylater.domain.model.NotificationStatus
import ao.techify.notifylater.domain.repository.NotificationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * In-memory repository used by unit tests and by code paths that do not need
 * persistence. The Android application uses [PersistentNotificationRepository].
 *
 * Bounded to [MAX_STORED] entries (oldest dropped). The Android application
 * uses a persistent repository that never drops pending entries.
 */
class NotificationRepositoryImpl(
    private val maxStored: Int = MAX_STORED
) : NotificationRepository {

    private val lock = Any()
    private val _notifications = MutableStateFlow<List<Notification>>(emptyList())
    override val notifications: Flow<List<Notification>> = _notifications

    override suspend fun save(notification: Notification): Notification = synchronized(lock) {
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
        val withoutSameId = current.filterNot { it.id == stored.id }
        _notifications.value = trim(withoutSameId + stored)
        stored
    }

    override suspend fun update(notification: Notification) = synchronized(lock) {
        val current = _notifications.value
        if (current.none { it.id == notification.id }) return@synchronized
        _notifications.value = trim(current.map { if (it.id == notification.id) notification else it })
    }

    override suspend fun findById(id: String): Notification? = synchronized(lock) {
        _notifications.value.firstOrNull { it.id == id }
    }

    override suspend fun clear() = synchronized(lock) {
        _notifications.value = emptyList()
    }

    override suspend fun clearCompleted() = synchronized(lock) {
        _notifications.value = _notifications.value.filter {
            it.status == NotificationStatus.PENDING
        }
    }

    private fun trim(items: List<Notification>): List<Notification> =
        items.sortedBy { it.timestamp }.takeLast(maxStored)

    companion object {
        const val MAX_STORED = 50
    }
}
