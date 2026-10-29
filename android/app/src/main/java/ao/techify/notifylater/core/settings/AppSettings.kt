package ao.techify.notifylater.core.settings

/**
 * User settings that affect notification capture and delivery.
 */
data class AppSettings(
    val onboardingCompleted: Boolean = false,
    val language: String = "en",
    val enabled: Boolean = true,
    val deliveryDelayMillis: Long = DEFAULT_DELIVERY_DELAY_MILLIS
) {
    companion object {
        const val DEFAULT_DELIVERY_DELAY_MILLIS = 60_000L
        const val MIN_DELIVERY_DELAY_MILLIS = 10_000L
        const val MAX_DELIVERY_DELAY_MILLIS = 24 * 60 * 60_000L

        /** Presets shown in Settings. */
        val DELAY_OPTIONS_MILLIS = listOf(
            30_000L,
            60_000L,
            5 * 60_000L,
            15 * 60_000L,
            60 * 60_000L
        )

        fun normalizeDelay(delayMillis: Long): Long =
            delayMillis.coerceIn(MIN_DELIVERY_DELAY_MILLIS, MAX_DELIVERY_DELAY_MILLIS)
    }
}
