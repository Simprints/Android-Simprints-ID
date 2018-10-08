package com.simprints.id.testTools

import android.os.SystemClock
import android.support.test.espresso.IdlingPolicies.getMasterIdlingPolicy
import android.support.test.espresso.IdlingPolicies.setMasterPolicyTimeout
import android.support.test.espresso.matcher.BoundedMatcher
import android.view.View
import android.widget.ProgressBar
import com.schibsted.spain.barista.interaction.BaristaSleepInteractions.sleep
import org.hamcrest.Description
import org.hamcrest.Matcher
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


fun withProgressBarValue(expectedValue: Int): Matcher<View> {
    return object : BoundedMatcher<View, ProgressBar>(ProgressBar::class.java) {

        override fun describeTo(description: Description) {
            description.appendText("Checking the matcher on received view: ")
            description.appendText("with progressBarValue=$expectedValue")
        }

        override fun matchesSafely(progressBar: ProgressBar): Boolean {
            return progressBar.progress == 0
        }
    }
}
