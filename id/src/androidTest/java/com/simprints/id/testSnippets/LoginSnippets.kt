package com.simprints.id.testSnippets

import android.support.test.espresso.Espresso
import android.support.test.espresso.action.ViewActions
import android.support.test.espresso.assertion.ViewAssertions
import android.support.test.espresso.matcher.ViewMatchers
import android.support.test.rule.ActivityTestRule
import com.simprints.id.R
import com.simprints.id.activities.checkLogin.openedByIntent.CheckLoginFromIntentActivity
import com.simprints.id.testTools.*
import com.simprints.id.tools.*
import com.simprints.libsimprints.Constants
import org.junit.Assert


fun launchAppFromIntentEnrol(calloutCredentials: CalloutCredentials,
                             enrolTestRule: ActivityTestRule<CheckLoginFromIntentActivity>) {
    log("launchAppFromIntentEnrol")
    ActivityUtils.launchActivityAndRunOnUiThread(calloutCredentials,
        Constants.SIMPRINTS_REGISTER_INTENT, enrolTestRule)
}

fun enterCredentialsDirectly(calloutCredentials: CalloutCredentials, projectSecret: String) {
    log("enterCredentialsDirectly")
    WaitingUtils.tryOnUiUntilTimeout(1000, 50) {
        Espresso.onView(ViewMatchers.withId(R.id.loginEditTextProjectId))
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
            .perform(ViewActions.typeText(calloutCredentials.projectId))
            .perform(ViewActions.closeSoftKeyboard())
        Espresso.onView(ViewMatchers.withId(R.id.loginEditTextProjectSecret))
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
            .perform(ViewActions.typeText(projectSecret))
            .perform(ViewActions.closeSoftKeyboard())
    }
}

fun pressSignIn() {
    log("pressSignIn")
    Espresso.onView(ViewMatchers.withId(R.id.loginButtonSignIn))
        .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
        .check(ViewAssertions.matches(ViewMatchers.isClickable()))
        .perform(ViewActions.click())
}

fun ensureSignInSuccess(calloutCredentials: CalloutCredentials, activityTestRule: ActivityTestRule<*>) {
    log("ensureSignInSuccess")
    WaitingUtils.tryOnUiUntilTimeout(25000, 1000) {
        Assert.assertTrue(AppUtils.getApp(activityTestRule).dataManager.isSignedIn(calloutCredentials.projectId, calloutCredentials.userId))
    }
}

fun ensureSignInFailure(calloutCredentials: CalloutCredentials, activityTestRule: ActivityTestRule<*>) {
    log("ensureSignInFailure")
    WaitingUtils.tryOnUiUntilTimeout(25000, 1000) {
        Assert.assertFalse(AppUtils.getApp(activityTestRule).dataManager.isSignedIn(calloutCredentials.projectId, calloutCredentials.userId))
    }
}
