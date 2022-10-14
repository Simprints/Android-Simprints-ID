package com.simprints.testtools.android

import android.os.SystemClock

fun tryOnSystemUntilTimeout(timeout: Long, pollingInterval: Long, snippet: () -> Any?): Any? =
    tryUntilTimeout(timeout, pollingInterval, snippet, ::waitOnSystem)

private fun tryUntilTimeout(
    timeout: Long,
    pollingInterval: Long,
    snippet: () -> Any?,
    waitingFunction: (Long) -> Unit
): Any? {
    for (runningTime in 0..timeout step pollingInterval) {
        try {
            return snippet()
        } catch (e: Throwable) {
        }
        waitingFunction(pollingInterval)
    }

    return snippet()
}

fun waitOnSystem(millis: Long) {
    SystemClock.sleep(millis)
}
