package ao.techify.notifylater.domain.repository

import ao.techify.notifylater.domain.model.Notification
import kotlinx.coroutines.flow.Flow

interface NotificationRepository {
    val notifications: Flow<List<Notification>>

    /**
     * Persists a captured notification. A pending notification with the same
     * original Android key is updated in place so chat updates do not create
     * duplicate queue entries and keep their original delivery time.
     *
     * Returns the notification as stored.
     */
    suspend fun save(notification: Notification): Notification

    /** Replaces an existing notification by id (used by the delivery worker). */
    suspend fun update(notification: Notification)

    suspend fun findById(id: String): Notification?

    /** Removes every notification, including pending ones. */
    suspend fun clear()

    /** Removes delivered/dismissed history but keeps pending notifications. */
    suspend fun clearCompleted()
}
