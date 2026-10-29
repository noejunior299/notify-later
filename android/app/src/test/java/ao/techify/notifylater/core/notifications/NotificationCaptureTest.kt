package ao.techify.notifylater.core.notifications

import ao.techify.notifylater.domain.model.NotificationStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * JVM tests for the pure capture/transformation logic.
 * No Android framework behavior is exercised here.
 */
class NotificationCaptureTest {

    private val selfPackage = "ao.techify.notifylater"

    private fun raw(
        sourcePackage: String = "com.example.chat",
        label: String? = "Example Chat",
        fallback: String? = sourcePackage,
        title: CharSequence? = "Hello",
        text: CharSequence? = "A message",
        timestamp: Long = 1_700_000_000_000L,
        key: String = "0|com.example.chat|1|null|1000",
        category: String? = "msg",
        priority: Int = 0,
        isOngoing: Boolean = false,
        isGroupSummary: Boolean = false
    ) = RawNotification(
        sourcePackage = sourcePackage,
        applicationLabel = label,
        fallbackApplicationLabel = fallback,
        title = title,
        text = text,
        timestamp = timestamp,
        notificationKey = key,
        category = category,
        priority = priority,
        isOngoing = isOngoing,
        isGroupSummary = isGroupSummary
    )

    // --- title and text ---

    @Test
    fun `capture keeps title and text when both present`() {
        val result = NotificationCapture.capture(raw(), selfPackage)

        assertNotNull(result)
        assertEquals("Hello", result!!.title)
        assertEquals("A message", result.text)
        assertEquals("Example Chat", result.sourceApplicationName)
        assertEquals("com.example.chat", result.sourcePackage)
    }

    // --- title only ---

    @Test
    fun `capture handles title only`() {
        val result = NotificationCapture.capture(raw(text = null), selfPackage)

        assertNotNull(result)
        assertEquals("Hello", result!!.title)
        assertNull(result.text)
    }

    // --- text only ---

    @Test
    fun `capture handles text only`() {
        val result = NotificationCapture.capture(raw(title = null), selfPackage)

        assertNotNull(result)
        assertNull(result!!.title)
        assertEquals("A message", result.text)
    }

    // --- empty content ---

    @Test
    fun `capture handles empty content with nulls and still captures`() {
        val result = NotificationCapture.capture(
            raw(title = "", text = "   "),
            selfPackage
        )

        assertNotNull(result)
        assertNull(result!!.title)
        assertNull(result.text)
        assertNotNull(result.id)
    }

    @Test
    fun `sanitizeText trims whitespace and treats blank as null`() {
        assertNull(NotificationCapture.sanitizeText(null, 100))
        assertNull(NotificationCapture.sanitizeText("", 100))
        assertNull(NotificationCapture.sanitizeText("   ", 100))
        assertEquals("hi", NotificationCapture.sanitizeText("  hi  ", 100))
    }

    @Test
    fun `sanitizeText limits overly long content`() {
        val long = "x".repeat(500)
        val limited = NotificationCapture.sanitizeText(long, 200)
        assertEquals(200, limited!!.length)
    }

    // --- multiple notifications ---

    @Test
    fun `capture produces distinct ids for distinct notifications`() {
        val first = NotificationCapture.capture(raw(key = "k1"), selfPackage)!!
        val second = NotificationCapture.capture(raw(key = "k2"), selfPackage)!!

        assertTrue(first.id != second.id)
        assertEquals(first.timestamp, second.timestamp)
    }

    // --- missing application label ---

    @Test
    fun `missing application label falls back to package name`() {
        val result =
            NotificationCapture.capture(raw(label = null, fallback = "com.example.chat"), selfPackage)

        assertNotNull(result)
        assertEquals("com.example.chat", result!!.sourceApplicationName)
    }

    @Test
    fun `missing application label and no fallback yields null app name`() {
        val result = NotificationCapture.capture(raw(label = null, fallback = null), selfPackage)

        assertNotNull(result)
        assertNull(result!!.sourceApplicationName)
        assertEquals("com.example.chat", result.sourcePackage)
    }

    @Test
    fun `blank application label is treated as missing`() {
        val result = NotificationCapture.capture(raw(label = "   ", fallback = "pkg"), selfPackage)

        assertEquals("pkg", result!!.sourceApplicationName)
    }

    // --- timestamps ---

    @Test
    fun `timestamp is preserved`() {
        val ts = 1_712_345_678_901L
        val result = NotificationCapture.capture(raw(timestamp = ts), selfPackage)

        assertEquals(ts, result!!.timestamp)
    }

    // --- filtering own notifications ---

    @Test
    fun `self notifications are never captured`() {
        val selfRaw = raw(sourcePackage = selfPackage, label = "Notify Later")

        assertTrue(NotificationCapture.isSelfNotification(selfPackage, selfPackage))
        assertFalse(NotificationCapture.shouldCapture(selfRaw, selfPackage))
        assertNull(NotificationCapture.capture(selfRaw, selfPackage))
    }

    @Test
    fun `other packages are not treated as self`() {
        assertFalse(NotificationCapture.isSelfNotification("com.other.app", selfPackage))
        assertTrue(
            NotificationCapture.shouldCapture(
                raw(sourcePackage = "com.other.app"),
                selfPackage
            )
        )
    }

    // --- invalid input fails safe ---

    @Test
    fun `missing key prevents capture`() {
        assertNull(NotificationCapture.capture(raw(key = ""), selfPackage))
    }

    @Test
    fun `zero timestamp prevents capture`() {
        assertNull(NotificationCapture.capture(raw(timestamp = 0L), selfPackage))
    }

    @Test
    fun `ongoing notifications are left untouched`() {
        assertNull(NotificationCapture.capture(raw(isOngoing = true), selfPackage))
    }

    @Test
    fun `group summaries are left untouched`() {
        assertNull(NotificationCapture.capture(raw(isGroupSummary = true), selfPackage))
    }

    @Test
    fun `calls alarms and services are left untouched`() {
        listOf("call", "alarm", "service", "CALL").forEach { category ->
            assertNull(
                "category $category should not be delayed",
                NotificationCapture.capture(raw(category = category), selfPackage)
            )
        }
    }

    @Test
    fun `normal message categories are still captured`() {
        assertNotNull(NotificationCapture.capture(raw(category = "msg"), selfPackage))
        assertNotNull(NotificationCapture.capture(raw(category = null), selfPackage))
    }

    @Test
    fun `system chrome notifications are left untouched`() {
        listOf("android", "com.android.systemui").forEach { pkg ->
            assertNull(
                "$pkg notifications should not be delayed",
                NotificationCapture.capture(raw(sourcePackage = pkg), selfPackage)
            )
        }
    }

    @Test
    fun `negative timestamp prevents capture`() {
        assertNull(NotificationCapture.capture(raw(timestamp = -1L), selfPackage))
    }

    // --- renderer host package contract ---

    /**
     * The renderer posts from Notify Later's own package (see applicationId in
     * app/build.gradle.kts). That package must never be capturable, otherwise
     * re-published notifications would be recursively captured.
     */
    @Test
    fun `renderer host package can never be captured`() {
        val rendererHostPackage = "ao.techify.notifylater"

        assertEquals(selfPackage, rendererHostPackage)
        assertTrue(NotificationCapture.isSelfNotification(rendererHostPackage, selfPackage))
        assertNull(
            NotificationCapture.capture(
                raw(sourcePackage = rendererHostPackage),
                selfPackage
            )
        )
    }

    // --- valid external notification contract ---

    @Test
    fun `external package with all required fields yields a Notification`() {
        val result = NotificationCapture.capture(raw(), selfPackage)

        assertNotNull(result)
        val notification = result!!
        assertEquals("com.example.chat", notification.sourcePackage)
        assertEquals(1_700_000_000_000L, notification.timestamp)
    }

    // --- metadata ---

    @Test
    fun `category and priority are carried over`() {
        val result = NotificationCapture.capture(raw(category = "msg", priority = 2), selfPackage)

        assertEquals("msg", result!!.category)
        assertEquals(2, result.priority)
        assertEquals(NotificationStatus.PENDING, result.status)
    }
}
