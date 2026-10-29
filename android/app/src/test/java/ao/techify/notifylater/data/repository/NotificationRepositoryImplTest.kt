package ao.techify.notifylater.data.repository

import ao.techify.notifylater.core.notifications.NotificationCapture
import ao.techify.notifylater.core.notifications.RawNotification
import ao.techify.notifylater.domain.model.NotificationStatus
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * In-memory repository behavior for the Phase 1 spike.
 */
class NotificationRepositoryImplTest {

    private fun captured(index: Int): ao.techify.notifylater.domain.model.Notification =
        NotificationCapture.capture(
            RawNotification(
                sourcePackage = "com.example.app$index",
                applicationLabel = "App $index",
                title = "Title $index",
                text = "Text $index",
                timestamp = 1_700_000_000_000L + index,
                notificationKey = "key-$index"
            ),
            selfPackage = "ao.techify.notifylater"
        )!!

    @Test
    fun `save stores multiple notifications in order`() = runBlocking {
        val repo = NotificationRepositoryImpl()

        repo.save(captured(1))
        repo.save(captured(2))
        repo.save(captured(3))

        val items = repo.notifications.first()
        assertEquals(3, items.size)
        assertEquals("com.example.app1", items[0].sourcePackage)
        assertEquals("com.example.app3", items[2].sourcePackage)
    }

    @Test
    fun `repository is bounded and drops oldest entries`() = runBlocking {
        val repo = NotificationRepositoryImpl(maxStored = 5)

        repeat(10) { index -> repo.save(captured(index)) }

        val items = repo.notifications.first()
        assertEquals(5, items.size)
        assertTrue(items.all { it.sourcePackage >= "com.example.app5" })
    }

    @Test
    fun `clear removes everything`() = runBlocking {
        val repo = NotificationRepositoryImpl()
        repo.save(captured(1))

        repo.clear()

        assertEquals(0, repo.notifications.first().size)
    }

    @Test
    fun `save returns the stored notification`() = runBlocking {
        val repo = NotificationRepositoryImpl()
        val input = captured(1).copy(dueAt = 1_700_000_100_000L)

        val stored = repo.save(input)

        assertEquals(input, stored)
        assertEquals(stored, repo.findById(input.id))
    }

    @Test
    fun `pending update with the same key keeps the original due time`() = runBlocking {
        val repo = NotificationRepositoryImpl()
        val first = captured(1).copy(dueAt = 1_700_000_100_000L)
        repo.save(first)

        val update = first.copy(title = "Updated title", dueAt = 1_700_000_200_000L)
        val stored = repo.save(update)

        assertEquals(1, repo.notifications.first().size)
        assertEquals(first.id, stored.id)
        assertEquals(first.dueAt, stored.dueAt)
        assertEquals("Updated title", stored.title)
    }

    @Test
    fun `update replaces status and delivery time`() = runBlocking {
        val repo = NotificationRepositoryImpl()
        val pending = captured(1).copy(dueAt = 1_700_000_100_000L)
        repo.save(pending)

        val delivered = pending.copy(
            status = NotificationStatus.DELIVERED,
            deliveredAt = 1_700_000_200_000L
        )
        repo.update(delivered)

        val stored = repo.findById(pending.id)
        assertEquals(NotificationStatus.DELIVERED, stored?.status)
        assertEquals(1_700_000_200_000L, stored?.deliveredAt)
    }

    @Test
    fun `clearCompleted keeps pending notifications`() = runBlocking {
        val repo = NotificationRepositoryImpl()
        val pending = captured(1).copy(dueAt = 1_700_000_100_000L)
        repo.save(pending)
        repo.save(
            captured(2).copy(
                status = NotificationStatus.DELIVERED,
                deliveredAt = 1_700_000_200_000L
            )
        )

        repo.clearCompleted()

        val items = repo.notifications.first()
        assertEquals(1, items.size)
        assertEquals(pending.id, items.single().id)
    }
}
