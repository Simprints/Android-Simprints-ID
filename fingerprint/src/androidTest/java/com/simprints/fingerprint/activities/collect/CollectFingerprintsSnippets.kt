package com.simprints.fingerprint.activities.collect

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.RootMatchers.isDialog
import androidx.test.espresso.matcher.ViewMatchers.*
import com.simprints.fingerprint.R
import com.simprints.testtools.android.WaitingUtils.UI_POLLING_INTERVAL_LONG
import com.simprints.testtools.android.WaitingUtils.UI_POLLING_INTERVAL_SHORT
import com.simprints.testtools.android.WaitingUtils.UI_TIMEOUT
import com.simprints.testtools.android.getResourceString
import com.simprints.testtools.android.log
import com.simprints.testtools.android.tryOnUiUntilTimeout
import com.simprints.testtools.android.waitOnUi
import org.hamcrest.Matchers.not

fun waitUntilCollectFingerprintsIsDisplayed() {
    tryOnUiUntilTimeout(UI_TIMEOUT, UI_POLLING_INTERVAL_LONG) {
        onView(withId(R.id.scan_button)).check(matches(isDisplayed()))
    }
}

/** Work around for reconnecting dialog that pops up sometimes when using SimulatedScannerManager */
fun pressScanUntilDialogIsDisplayedAndClickConfirm(dialogResult: String = "✓ LEFT THUMB\n✓ LEFT INDEX FINGER\n") {
    log("CollectFingerprints::pressScanUntilDialogIsDisplayedAndClickConfirm")

    try {
        repeat(10) {
            onView(withId(R.id.scan_button))
                .check(matches(not(withText(R.string.cancel_button))))
                .perform(click())
            waitOnUi(500)
        }
    } catch (e: Throwable) {}

    checkIfDialogIsDisplayedWithResultAndClickConfirm(dialogResult)
}

fun checkIfDialogIsDisplayedWithResultAndClickConfirm(dialogResult: String = "✓ LEFT THUMB\n✓ LEFT INDEX FINGER\n") {
    log("CollectFingerprints::checkIfDialogIsDisplayedWithResultAndClickConfirm")
    tryOnUiUntilTimeout(UI_TIMEOUT, UI_POLLING_INTERVAL_SHORT) {
        onView(withText(getResourceString(R.string.confirm_fingers_dialog_title)))
            .inRoot(isDialog())
            .check(matches(isDisplayed()))
        onView(withId(android.R.id.message))
            .inRoot(isDialog())
            .check(matches(withText(dialogResult)))
            .check(matches(isDisplayed()))
        onView(withId(android.R.id.button1)).perform(click())
    }
}

