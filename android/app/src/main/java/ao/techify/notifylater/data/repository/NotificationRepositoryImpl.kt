package ao.techify.notifylater.data.repository

import ao.techify.notifylater.domain.model.Notification
import ao.techify.notifylater.domain.repository.NotificationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * In-memory repository for the Phase 1 spike.
 * No persistence by design: notification content is sensitive and must not be
 * stored longer than needed. Bounded to [MAX_STORED] entries (oldest dropped).
 * Will be replaced by Room in Phase 2.
 */
class NotificationRepositoryImpl(
    private val maxStored: Int = MAX_STORED
) : NotificationRepository {

    private val _notifications = MutableStateFlow<List<Notification>>(emptyList())
    override val notifications: Flow<List<Notification>> = _notifications

    override suspend fun save(notification: Notification) {
        val updated = (_notifications.value + notification).takeLast(maxStored)
        _notifications.value = updated
    }

    override suspend fun clear() {
        _notifications.value = emptyList()
    }

    companion object {
        const val MAX_STORED = 50
    }
}
