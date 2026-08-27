package ao.techify.notifylater.domain.usecase

import ao.techify.notifylater.domain.model.Notification
import ao.techify.notifylater.domain.repository.NotificationRepository
import kotlinx.coroutines.flow.Flow

class GetNotificationsUseCase(
    private val repository: NotificationRepository
) {
    operator fun invoke(): Flow<List<Notification>> = repository.notifications
}
