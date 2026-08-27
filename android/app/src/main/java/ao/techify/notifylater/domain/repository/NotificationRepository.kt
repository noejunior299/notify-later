package ao.techify.notifylater.domain.repository

import ao.techify.notifylater.domain.model.Notification
import kotlinx.coroutines.flow.Flow

interface NotificationRepository {
    val notifications: Flow<List<Notification>>
    suspend fun save(notification: Notification)
    suspend fun clear()
}
