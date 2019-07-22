package com.simprints.fingerprint.activities.collectfingerprint

import android.app.Activity
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.RootMatchers.isDialog
import androidx.test.espresso.matcher.RootMatchers.withDecorView
import androidx.test.espresso.matcher.ViewMatchers.*
import com.simprints.fingerprint.R
import com.simprints.testtools.android.WaitingUtils.UI_POLLING_INTERVAL_LONG
import com.simprints.testtools.android.WaitingUtils.UI_POLLING_INTERVAL_SHORT
import com.simprints.testtools.android.WaitingUtils.UI_TIMEOUT
import com.simprints.testtools.android.getResourceString
import com.simprints.testtools.android.log
import com.simprints.testtools.android.tryOnUiUntilTimeout
import com.simprints.testtools.android.waitOnUi
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.not

fun pressScan() {
    log("CollectFingerprints::pressScan")
    tryOnUiUntilTimeout(UI_TIMEOUT, UI_POLLING_INTERVAL_LONG) {
        onView(withId(R.id.scan_button))
            .check(matches(not(withText(R.string.cancel_button))))
            .perform(click())
    }
    waitOnUi(500) // Wait for ViewPager animation
}

fun skipFinger() {
    log("CollectFingerprints::skipFinger")
    tryOnUiUntilTimeout(UI_TIMEOUT, UI_POLLING_INTERVAL_LONG) {
        onView(withId(R.id.missingFingerText))
            .check(matches(isDisplayed()))
            .perform(click())
    }
}

fun waitForSplashScreenToAppearAndDisappear() {
    log("CollectFingerprints::waitForSplashScreenToAppearAndDisappear")
    tryOnUiUntilTimeout(UI_TIMEOUT, UI_POLLING_INTERVAL_LONG) {
        onView(withId(R.id.splashGetReady))
            .check(matches(isDisplayed()))
    }

    tryOnUiUntilTimeout(UI_TIMEOUT, UI_POLLING_INTERVAL_LONG) {
        onView(withId(R.id.splashGetReady))
            .check(doesNotExist())
    }

    waitOnUi(500)
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

fun checkIfDialogIsDisplayedAndClickRestart() {
    log("CollectFingerprints::checkIfDialogIsDisplayedAndClickRestart")
    tryOnUiUntilTimeout(UI_TIMEOUT, UI_POLLING_INTERVAL_SHORT) {
        onView(withText(getResourceString(R.string.confirm_fingers_dialog_title)))
            .inRoot(isDialog())
            .check(matches(isDisplayed()))
        onView(withId(android.R.id.button2)).perform(click())
    }
}

fun checkNoFingersScannedToastIsShown(activity: Activity) {
    log("CollectFingerprints::checkNoFingersScannedToastIsShown")
    tryOnUiUntilTimeout(UI_TIMEOUT, UI_POLLING_INTERVAL_SHORT) {
        onView(withText(getResourceString(R.string.no_fingers_scanned)))
            .inRoot(withDecorView(not(`is`(activity.window.decorView))))
            .check(matches(isDisplayed()))
    }
}

fun checkFirstFingerYetToBeScanned() {
    log("CollectFingerprints::checkFirstFingerYetToBeScanned")
    tryOnUiUntilTimeout(UI_TIMEOUT, UI_POLLING_INTERVAL_LONG) {
        onView(withId(R.id.scan_button))
            .check(matches(withText(R.string.scan_label)))
    }
}
