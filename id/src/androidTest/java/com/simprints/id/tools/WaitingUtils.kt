package com.simprints.id.tools

import android.os.SystemClock
import android.support.test.espresso.IdlingPolicies.getMasterIdlingPolicy
import android.support.test.espresso.IdlingPolicies.setMasterPolicyTimeout
import com.schibsted.spain.barista.BaristaSleepActions.sleep
import java.util.concurrent.TimeUnit


object WaitingUtils {

    fun tryOnUiUntilTimeout(timeout: Long, pollingInterval: Long, snippet: () -> Any?) {
        changeUiTimeoutPolicyIfNeeded(timeout)
        tryUntilTimeout(timeout, pollingInterval, snippet, ::waitOnUi)
    }

    fun tryOnSystemUntilTimeout(timeout: Long, pollingInterval: Long, snippet: () -> Any?) {
        tryUntilTimeout(timeout, pollingInterval, snippet, ::waitOnSystem)
    }

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

    private fun waitOnUi(millis: Long) {
        sleep(millis, TimeUnit.MILLISECONDS)
    }

    private fun waitOnSystem(millis: Long) {
        SystemClock.sleep(millis)
    }

    private fun changeUiTimeoutPolicyIfNeeded(timeout: Long) {
        val idlingPolicy = getMasterIdlingPolicy()
        val currentTimeoutMillis = TimeUnit.MILLISECONDS.convert(idlingPolicy.idleTimeout, idlingPolicy.idleTimeoutUnit)
        if (currentTimeoutMillis <= timeout)
            setMasterPolicyTimeout(timeout * 2, TimeUnit.MILLISECONDS)
    }
}
