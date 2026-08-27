package ao.techify.notifylater.domain.model

/**
 * Per-app or global rule describing when to hold vs deliver.
 * See docs/architecture/rules-engine.md
 */
data class NotificationRule(
    val packageName: String?, // null = global
    val delayPolicy: DelayPolicy
)
