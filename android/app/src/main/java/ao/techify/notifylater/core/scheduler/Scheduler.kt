package ao.techify.notifylater.core.scheduler

/**
 * Scheduler abstraction for delayed delivery.
 * Concrete implementation (WorkManager / AlarmManager) will be added in Phase 2.
 */
interface Scheduler {
    fun schedule(delayMillis: Long)
    fun cancel()
}

class NoOpScheduler : Scheduler {
    override fun schedule(delayMillis: Long) = Unit
    override fun cancel() = Unit
}
