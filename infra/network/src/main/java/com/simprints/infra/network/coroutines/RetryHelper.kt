package com.simprints.infra.network.coroutines

import kotlinx.coroutines.delay

internal suspend fun <T> retryIO(
    times: Int = Int.MAX_VALUE,
    initialDelay: Long = 100, // 0.1 second
    maxDelay: Long = 1000, // 1 second
    factor: Double = 1.0, // 2 per exponential backoff
    runBlock: suspend () -> T,
    retryIf: suspend (t: Throwable) -> Boolean = { true },
): T {
    var currentDelay = initialDelay
    repeat(times - 1) {
        try {
            return runBlock()
        } catch (t: Throwable) {
            if (!retryIf(t)) {
                throw t
            }
        }
        delay(currentDelay)
        currentDelay = (currentDelay * factor).toLong().coerceAtMost(maxDelay)
    }
    return runBlock() // last attempt
}
