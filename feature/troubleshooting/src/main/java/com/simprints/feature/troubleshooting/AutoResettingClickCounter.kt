package com.simprints.feature.troubleshooting

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicInteger

class AutoResettingClickCounter(
    private val requiredClicks: Int = DEFAULT_CLICKS_FOR_TROUBLESHOOTING,
    private val resetDelayMs: Long = DEFAULT_CLICK_COUNTER_RESET_MS,
) {
    private var counterReset: Job? = null
    private var counter: AtomicInteger = AtomicInteger(0)

    /**
     * Returns true if counter has reached required clicks.
     *
     * Counter gets reset after the delay has passed.
     */
    fun handleClick(resetScope: CoroutineScope): Boolean {
        counter.incrementAndGet()
        if (counter.compareAndSet(requiredClicks, 0)) {
            return true
        } else {
            counterReset?.cancel()
            counterReset = resetScope.launch {
                delay(resetDelayMs)
                if (isActive) counter.set(0)
            }
            return false
        }
    }

    companion object {
        private const val DEFAULT_CLICKS_FOR_TROUBLESHOOTING = 5
        private const val DEFAULT_CLICK_COUNTER_RESET_MS = 1000L
    }
}
