package ao.techify.notifylater.domain.usecase

import ao.techify.notifylater.domain.model.Notification
import ao.techify.notifylater.domain.repository.NotificationRepository

class SaveNotificationUseCase(
    private val repository: NotificationRepository
) {
    suspend operator fun invoke(notification: Notification) {
        repository.save(notification)
    }
}
