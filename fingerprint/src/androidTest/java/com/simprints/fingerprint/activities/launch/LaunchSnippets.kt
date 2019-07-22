package com.simprints.fingerprint.activities.launch

import androidx.test.espresso.Espresso
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.ViewMatchers
import com.simprints.fingerprint.R
import com.simprints.fingerprint.testtools.grantPermissions
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
        grantPermissions()
        Espresso.onView(ViewMatchers.withId(R.id.generalConsentTextView))
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
    }
}

private fun setupActivityContinue() {
    log("setupActivityContinue")
    tryOnUiUntilTimeout(WaitingUtils.UI_TIMEOUT, WaitingUtils.UI_POLLING_INTERVAL_LONG) {
        Espresso.onView(ViewMatchers.withId(R.id.consentAcceptButton))
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
            .perform(ViewActions.click())
    }
}

fun setupActivityDecline() {
    log("setupActivityContinue")
    tryOnUiUntilTimeout(WaitingUtils.UI_TIMEOUT, WaitingUtils.UI_POLLING_INTERVAL_LONG) {
        Espresso.onView(ViewMatchers.withId(R.id.consentDeclineButton))
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
            .perform(ViewActions.click())
    }
}
