package ao.techify.notifylater.core.settings

import android.content.Context
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Per-app delivery delay overrides.
 *
 * A package without a rule follows the global delay in
 * [AppSettings.deliveryDelayMillis]. Rules only affect notifications captured
 * after the rule is saved; already queued items keep their original due time.
 */
interface AppDelayRulesRepository {
    val rules: Flow<Map<String, Long>>
    val current: Map<String, Long>
    suspend fun setDelay(packageName: String, delayMillis: Long?)
}

/** Pure helpers for resolving, validating and encoding per-app rules. */
object AppDelayRules {

    /**
     * Delay for a package: its own rule when present, otherwise the global
     * delay. Values are always within [AppSettings] bounds after [withDelay].
     */
    fun resolve(
        rules: Map<String, Long>,
        packageName: String?,
        globalDelayMillis: Long
    ): Long = rules[packageName] ?: globalDelayMillis

    /** Returns a copy with the rule set, or with the rule removed when [delayMillis] is null. */
    fun withDelay(
        rules: Map<String, Long>,
        packageName: String?,
        delayMillis: Long?
    ): Map<String, Long> {
        if (packageName.isNullOrBlank()) return rules
        if (delayMillis == null) return rules - packageName
        return rules + (packageName to AppSettings.normalizeDelay(delayMillis))
    }

    /** Stable, compact encoding: `package=millis;package=millis`. */
    fun encode(rules: Map<String, Long>): String = rules
        .filterKeys { it.isNotBlank() }
        .toSortedMap()
        .entries
        .joinToString(ENTRY_SEPARATOR.toString()) { entry ->
            "${entry.key}$KEY_VALUE_SEPARATOR${entry.value}"
        }

    /** Inverse of [encode]; malformed or out-of-bounds entries are dropped. */
    fun decode(raw: String?): Map<String, Long> {
        if (raw.isNullOrBlank()) return emptyMap()
        return raw.split(ENTRY_SEPARATOR).mapNotNull { entry ->
            val separatorIndex = entry.lastIndexOf(KEY_VALUE_SEPARATOR)
            if (separatorIndex <= 0) return@mapNotNull null
            val packageName = entry.substring(0, separatorIndex).trim()
            val delayMillis = entry.substring(separatorIndex + 1).toLongOrNull()
                ?: return@mapNotNull null
            if (packageName.isEmpty()) return@mapNotNull null
            if (delayMillis !in AppSettings.MIN_DELIVERY_DELAY_MILLIS..AppSettings.MAX_DELIVERY_DELAY_MILLIS) {
                return@mapNotNull null
            }
            packageName to delayMillis
        }.toMap()
    }

    private const val ENTRY_SEPARATOR = ';'
    private const val KEY_VALUE_SEPARATOR = '='
}

/** In-memory rules for tests and previews. */
class InMemoryAppDelayRulesRepository : AppDelayRulesRepository {
    private val _rules = MutableStateFlow<Map<String, Long>>(emptyMap())
    override val rules: Flow<Map<String, Long>> = _rules.asStateFlow()
    override val current: Map<String, Long> get() = _rules.value

    override suspend fun setDelay(packageName: String, delayMillis: Long?) {
        _rules.value = AppDelayRules.withDelay(_rules.value, packageName, delayMillis)
    }
}

/**
 * Local-only persisted rules. Keeps per-app delays across process restarts
 * without introducing a database for a single map.
 */
class PersistentAppDelayRulesRepository(context: Context) : AppDelayRulesRepository {

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val _rules = MutableStateFlow(AppDelayRules.decode(prefs.getString(KEY_RULES, null)))
    override val rules: Flow<Map<String, Long>> = _rules.asStateFlow()
    override val current: Map<String, Long> get() = _rules.value

    override suspend fun setDelay(packageName: String, delayMillis: Long?) {
        val next = AppDelayRules.withDelay(_rules.value, packageName, delayMillis)
        prefs.edit().putString(KEY_RULES, AppDelayRules.encode(next)).apply()
        _rules.value = next
    }

    companion object {
        private const val PREFS_NAME = "notify_later_app_rules"
        private const val KEY_RULES = "delay_rules"
    }
}
