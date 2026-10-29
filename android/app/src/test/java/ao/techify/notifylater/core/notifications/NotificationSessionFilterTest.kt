package ao.techify.notifylater.core.notifications

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * JVM tests for the connection baseline that prevents already-active
 * notifications from being re-published by the Phase 1 spike.
 */
class NotificationSessionFilterTest {

    private val selfPackage = "ao.techify.notifylater"
    private lateinit var session: NotificationSessionFilter

    @Before
    fun setUp() {
        session = NotificationSessionFilter(selfPackage)
    }

    // --- 1. existing notification at listener connection -> ignored ---

    @Test
    fun `notification active at connection is ignored`() {
        session.onConnected(listOf("0|com.gmail|1|null|1000"))

        val decision = session.decide("com.gmail", "0|com.gmail|1|null|1000")

        assertEquals(NotificationSessionFilter.Decision.IgnoreInitial, decision)
    }

    // --- 2. new notification after connection -> captured ---

    @Test
    fun `genuinely new notification after connection is processed`() {
        session.onConnected(listOf("baseline-key"))

        val decision = session.decide("com.example.chat", "new-key")

        assertEquals(NotificationSessionFilter.Decision.Process, decision)
    }

    // --- 3. self notification -> ignored (always wins) ---

    @Test
    fun `self notification is ignored even when not in baseline`() {
        session.onConnected(emptyList())

        val decision = session.decide(selfPackage, "own-notification-key")

        assertEquals(NotificationSessionFilter.Decision.IgnoreSelf, decision)
    }

    @Test
    fun `self notification is ignored even before connection`() {
        val decision = session.decide(selfPackage, "own-notification-key")

        assertEquals(NotificationSessionFilter.Decision.IgnoreSelf, decision)
    }

    // --- 4. existing notification delivered multiple times -> never re-published ---

    @Test
    fun `initial active notification stays suppressed across repeated deliveries`() {
        session.onConnected(
            mapOf("0|com.android.shell|7|null|42" to "same-content-hash")
        )

        repeat(5) {
            val decision = session.decide(
                "com.android.shell",
                "0|com.android.shell|7|null|42",
                signature = "same-content-hash"
            )
            assertEquals(NotificationSessionFilter.Decision.IgnoreInitial, decision)
        }
    }

    @Test
    fun `baseline key updated after connection is processed`() {
        val key = "0|com.whatsapp|7|chat|42"
        session.onConnected(
            mapOf(key to "title=old|text=first message|when=1000")
        )

        // Same notification key, but new message content: this is an update,
        // not the snapshot notification.
        val decision = session.decide(
            "com.whatsapp",
            key,
            signature = "title=old|text=second message|when=2000"
        )
        assertEquals(NotificationSessionFilter.Decision.Process, decision)

        // Once updated, the key is no longer treated as part of the baseline.
        assertEquals(
            NotificationSessionFilter.Decision.Process,
            session.decide("com.whatsapp", key, signature = "anything")
        )
    }

    @Test
    fun `baseline key with unchanged content is still ignored`() {
        val key = "0|com.whatsapp|7|chat|42"
        session.onConnected(mapOf(key to "same-signature"))

        assertEquals(
            NotificationSessionFilter.Decision.IgnoreInitial,
            session.decide("com.whatsapp", key, signature = "same-signature")
        )
    }

    // --- 5. same package but different key -> captured ---

    @Test
    fun `same package with a different key is processed`() {
        session.onConnected(listOf("0|com.example.chat|1|null|1000"))

        val decision = session.decide("com.example.chat", "0|com.example.chat|2|null|1001")

        assertEquals(NotificationSessionFilter.Decision.Process, decision)
    }

    // --- 6. empty/invalid key remains rejected by existing capture rules ---

    @Test
    fun `blank key passes the filter but is rejected by capture rules`() {
        session.onConnected(emptyList())

        assertEquals(
            NotificationSessionFilter.Decision.Process,
            session.decide("com.example.chat", "")
        )

        val raw = RawNotification(
            sourcePackage = "com.example.chat",
            timestamp = 1_700_000_000_000L,
            notificationKey = ""
        )
        assertEquals(null, NotificationCapture.capture(raw, selfPackage))
    }

    // --- connection lifecycle / race conditions ---

    @Test
    fun `notification before connection is ignored fail-safe`() {
        val decision = session.decide("com.example.chat", "some-key")

        assertFalse(session.isConnected())
        assertEquals(NotificationSessionFilter.Decision.IgnoreNotConnected, decision)
    }

    @Test
    fun `reconnection replaces the previous baseline`() {
        session.onConnected(listOf("old-baseline"))
        session.onConnected(listOf("new-baseline"))

        assertEquals(
            NotificationSessionFilter.Decision.Process,
            session.decide("com.example.chat", "old-baseline")
        )
        assertEquals(
            NotificationSessionFilter.Decision.IgnoreInitial,
            session.decide("com.example.chat", "new-baseline")
        )
    }

    @Test
    fun `reset clears baseline and disconnects`() {
        session.onConnected(listOf("key"))
        session.reset()

        assertFalse(session.isConnected())
        assertEquals(0, session.baselineCount())
        assertEquals(
            NotificationSessionFilter.Decision.IgnoreNotConnected,
            session.decide("com.example.chat", "key")
        )
    }

    @Test
    fun `null package and null key are handled without crashing`() {
        session.onConnected(emptyList())

        assertEquals(
            NotificationSessionFilter.Decision.Process,
            session.decide(null, null)
        )
    }

    @Test
    fun `blank baseline keys are never recorded`() {
        session.onConnected(listOf("", "   ", "valid-key"))
        assertEquals(1, session.baselineCount())

        // A blank key can therefore never be treated as initial; it is rejected
        // by NotificationCapture instead.
        assertEquals(
            NotificationSessionFilter.Decision.Process,
            session.decide("com.example.chat", "")
        )
    }
}
