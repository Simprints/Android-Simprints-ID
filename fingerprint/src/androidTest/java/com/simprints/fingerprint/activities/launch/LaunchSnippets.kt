package com.simprints.fingerprint.activities.launch

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import com.simprints.fingerprint.R
import com.simprints.testtools.android.WaitingUtils
import com.simprints.testtools.android.log
import com.simprints.testtools.android.tryOnUiUntilTimeout

fun setupActivityAndContinue() {
    log("setupActivityAndContinue")
    setupActivity()
    setupActivityContinue()
}

fun setupActivityAndDecline() {
    log("setupActivityAndDecline")
    setupActivity()
    setupActivityDecline()
}

fun setupActivity() {
    log("setupActivity")
    tryOnUiUntilTimeout(WaitingUtils.UI_TIMEOUT, WaitingUtils.UI_POLLING_INTERVAL_SHORT) {
        onView(withId(R.id.generalConsentTextView))
            .check(matches(isDisplayed()))
    }
}

private fun setupActivityContinue() {
    log("setupActivityContinue")
    tryOnUiUntilTimeout(WaitingUtils.UI_TIMEOUT, WaitingUtils.UI_POLLING_INTERVAL_LONG) {
        onView(withId(R.id.consentAcceptButton))
            .check(matches(isDisplayed()))
            .perform(click())
    }
}

fun setupActivityDecline() {
    log("setupActivityContinue")
    tryOnUiUntilTimeout(WaitingUtils.UI_TIMEOUT, WaitingUtils.UI_POLLING_INTERVAL_LONG) {
        onView(withId(R.id.consentDeclineButton))
            .check(matches(isDisplayed()))
            .perform(click())
    }
}
