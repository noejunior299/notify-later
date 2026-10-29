package ao.techify.notifylater.core.settings

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * JVM tests for the per-app delay rule helpers: resolution against the global
 * delay, validation of stored values, and the storage encoding round-trip.
 */
class AppDelayRulesTest {

    private val global = AppSettings.DEFAULT_DELIVERY_DELAY_MILLIS

    // --- resolution ---

    @Test
    fun `per-app rule wins over the global delay`() {
        val rules = mapOf("com.whatsapp" to 30 * 60_000L)

        val delay = AppDelayRules.resolve(rules, "com.whatsapp", global)

        assertEquals(30 * 60_000L, delay)
    }

    @Test
    fun `package without rule falls back to global delay`() {
        val delay = AppDelayRules.resolve(
            mapOf("com.whatsapp" to 30 * 60_000L),
            "com.facebook.katana",
            global
        )

        assertEquals(global, delay)
    }

    @Test
    fun `null package falls back to global delay`() {
        assertEquals(global, AppDelayRules.resolve(emptyMap(), null, global))
    }

    @Test
    fun `different apps keep independent delays`() {
        val rules = mapOf(
            "com.whatsapp" to 30 * 60_000L,
            "com.facebook.katana" to 60 * 60_000L,
            "com.google.android.dialer" to 5 * 60_000L
        )

        assertEquals(30 * 60_000L, AppDelayRules.resolve(rules, "com.whatsapp", global))
        assertEquals(60 * 60_000L, AppDelayRules.resolve(rules, "com.facebook.katana", global))
        assertEquals(
            5 * 60_000L,
            AppDelayRules.resolve(rules, "com.google.android.dialer", global)
        )
        assertEquals(global, AppDelayRules.resolve(rules, "com.other.app", global))
    }

    // --- rule mutation and validation ---

    @Test
    fun `setDelay adds a rule and null removes it`() {
        var rules = AppDelayRules.withDelay(emptyMap(), "com.whatsapp", 30 * 60_000L)
        assertEquals(mapOf("com.whatsapp" to 30 * 60_000L), rules)

        rules = AppDelayRules.withDelay(rules, "com.whatsapp", null)
        assertTrue(rules.isEmpty())
    }

    @Test
    fun `blank package never creates a rule`() {
        val rules = AppDelayRules.withDelay(emptyMap(), "  ", 60_000L)
        assertTrue(rules.isEmpty())
    }

    @Test
    fun `stored values are normalized into the allowed bounds`() {
        val tooSmall = AppDelayRules.withDelay(emptyMap(), "com.app", 1L)
        assertEquals(AppSettings.MIN_DELIVERY_DELAY_MILLIS, tooSmall["com.app"])

        val tooLarge = AppDelayRules.withDelay(emptyMap(), "com.app", Long.MAX_VALUE)
        assertEquals(AppSettings.MAX_DELIVERY_DELAY_MILLIS, tooLarge["com.app"])
    }

    // --- encoding ---

    @Test
    fun `encode and decode round-trip`() {
        val rules = mapOf(
            "com.whatsapp" to 30 * 60_000L,
            "com.facebook.katana" to 60 * 60_000L,
            "com.google.android.dialer" to 5 * 60_000L
        )

        assertEquals(rules, AppDelayRules.decode(AppDelayRules.encode(rules)))
    }

    @Test
    fun `encoding is stable regardless of insertion order`() {
        val first = AppDelayRules.encode(
            linkedMapOf("com.b" to 60_000L, "com.a" to 120_000L)
        )
        val second = AppDelayRules.encode(
            linkedMapOf("com.a" to 120_000L, "com.b" to 60_000L)
        )

        assertEquals(first, second)
        assertEquals("com.a=120000;com.b=60000", first)
    }

    @Test
    fun `decode drops malformed and out-of-bounds entries`() {
        val decoded = AppDelayRules.decode(
            "com.good=60000;;=60000;no.separator;com.bad=abc;com.huge=999999999999;com.empty="
        )

        assertEquals(mapOf("com.good" to 60_000L), decoded)
    }

    @Test
    fun `decode of null blank or garbage is empty`() {
        assertTrue(AppDelayRules.decode(null).isEmpty())
        assertTrue(AppDelayRules.decode("").isEmpty())
        assertTrue(AppDelayRules.decode("   ").isEmpty())
        assertTrue(AppDelayRules.decode("???").isEmpty())
    }

    @Test
    fun `encode ignores blank keys`() {
        assertEquals("", AppDelayRules.encode(mapOf("" to 60_000L)))
    }
}
