package com.simprints.id.tools

import android.os.SystemClock
import android.support.test.espresso.IdlingPolicies.getMasterIdlingPolicy
import android.support.test.espresso.IdlingPolicies.setMasterPolicyTimeout
import com.schibsted.spain.barista.BaristaSleepActions.sleep
import java.util.concurrent.TimeUnit


object WaitingUtils {

    fun tryOnUiUntilTimeout(timeout: Long, pollingInterval: Long, snippet: () -> Unit) {
        changeUiTimeoutPolicyIfNeeded(timeout)
        tryUntilTimeout(timeout, pollingInterval, snippet, ::waitOnUi)
    }

    fun tryOnSystemUntilTimeout(timeout: Long, pollingInterval: Long, snippet: () -> Unit) {
        tryUntilTimeout(timeout, pollingInterval, snippet, ::waitOnSystem)
    }

    /**
     * This function will keep trying to execute snippet every pollingInterval milliseconds
     * (ignoring all errors), until timeout milliseconds is reached. Then it will execute snippet
     * one last time allowing it to fail and throw errors. It will return successfully the first
     * time snippet succeeds.
     */
    private tailrec fun tryUntilTimeout(timeout: Long,
                                        pollingInterval: Long,
                                        snippet: () -> Any?,
                                        waitingFunction: (Long) -> Unit,
                                        runningTime: Long = 0): Any? {
        if (runningTime >= timeout) {
            return snippet()
        }

        waitingFunction(pollingInterval)

        try {
            return snippet()
        } catch (e: Throwable) {
        }

        return tryUntilTimeout(timeout, pollingInterval, snippet, waitingFunction, runningTime + pollingInterval)
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
