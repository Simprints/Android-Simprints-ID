package com.simprints.testtools.android

import android.os.SystemClock
import androidx.test.espresso.IdlingPolicies.getMasterIdlingPolicy
import androidx.test.espresso.IdlingPolicies.setMasterPolicyTimeout
import com.schibsted.spain.barista.interaction.BaristaSleepInteractions.sleep
import java.util.concurrent.TimeUnit

fun tryOnUiUntilTimeout(timeout: Long, pollingInterval: Long, snippet: () -> Any?): Any? {
    changeUiTimeoutPolicyIfNeeded(timeout)
    return tryUntilTimeout(timeout, pollingInterval, snippet, ::waitOnUi)
}

fun tryOnSystemUntilTimeout(timeout: Long, pollingInterval: Long, snippet: () -> Any?): Any? =
    tryUntilTimeout(timeout, pollingInterval, snippet, ::waitOnSystem)

private fun tryUntilTimeout(timeout: Long,
                            pollingInterval: Long,
                            snippet: () -> Any?,
                            waitingFunction: (Long) -> Unit): Any? {
    for (runningTime in 0..timeout step pollingInterval) {
        try {
            return snippet()
        } catch (e: Throwable) {
        }
        waitingFunction(pollingInterval)
    }

    return snippet()
}

fun waitOnUi(millis: Long) {
    sleep(millis, TimeUnit.MILLISECONDS)
}

fun waitOnSystem(millis: Long) {
    SystemClock.sleep(millis)
}

private fun changeUiTimeoutPolicyIfNeeded(timeout: Long) {
    val idlingPolicy = getMasterIdlingPolicy()
    val currentTimeoutMillis = TimeUnit.MILLISECONDS.convert(idlingPolicy.idleTimeout, idlingPolicy.idleTimeoutUnit)
    if (currentTimeoutMillis <= timeout)
        setMasterPolicyTimeout(timeout * 2, TimeUnit.MILLISECONDS)
}

object WaitingUtils{
    const val UI_TIMEOUT = 20000L
    const val UI_POLLING_INTERVAL_SHORT = 50L
    const val UI_POLLING_INTERVAL_LONG = 200L
}
