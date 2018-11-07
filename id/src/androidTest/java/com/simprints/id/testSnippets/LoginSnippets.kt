package com.simprints.id.testSnippets

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.rule.ActivityTestRule
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import com.simprints.id.R
import com.simprints.id.activities.checkLogin.openedByIntent.CheckLoginFromIntentActivity
import com.simprints.id.testTools.ActivityUtils
import com.simprints.id.testTools.CalloutCredentials
import com.simprints.id.testTools.log
import com.simprints.id.testTools.tryOnUiUntilTimeout
import com.simprints.libsimprints.Constants

fun launchAppFromIntentEnrol(calloutCredentials: CalloutCredentials,
                             enrolTestRule: ActivityTestRule<CheckLoginFromIntentActivity>) {
    log("launchAppFromIntentEnrol")
    ActivityUtils.launchActivityAndRunOnUiThread(calloutCredentials,
        Constants.SIMPRINTS_REGISTER_INTENT, enrolTestRule)
}

fun launchAppFromIntentEnrolAndDoLogin(calloutCredentials: CalloutCredentials,
                                       loginTestRule: ActivityTestRule<CheckLoginFromIntentActivity>,
                                       projectSecret: String) {
    launchAppFromIntentEnrol(calloutCredentials, loginTestRule)
    enterCredentialsDirectly(calloutCredentials, projectSecret)
    pressSignIn()
    ensureSignInSuccess()
}

fun enterCredentialsDirectly(calloutCredentials: CalloutCredentials, projectSecret: String) {
    log("enterCredentialsDirectly")
    tryOnUiUntilTimeout(10000, 50) {
        onView(withId(R.id.loginEditTextProjectId))
            .check(matches(isDisplayed()))
            .perform(clearText())
            .perform(typeText(calloutCredentials.projectId))
            .perform(closeSoftKeyboard())
        onView(withId(R.id.loginEditTextProjectSecret))
            .check(matches(isDisplayed()))
            .perform(clearText())
            .perform(typeText(projectSecret))
            .perform(closeSoftKeyboard())
    }
}

fun pressSignIn() {
    log("pressSignIn")
    onView(withId(R.id.loginButtonSignIn))
        .check(matches(isDisplayed()))
        .check(matches(isClickable()))
        .perform(click())
}

fun ensureSignInSuccess() {
    log("ensureSignInSuccess")
    tryOnUiUntilTimeout(25000, 1000) {
        ActivityUtils.grantPermissions()
        onView(withId(R.id.consentAcceptButton))
            .check(matches(isDisplayed()))
    }
}

fun ensureSignInFailure() {
    log("ensureSignInFailure")
    tryOnUiUntilTimeout(25000, 1000) {
        onView(withId(R.id.loginButtonSignIn))
            .check(matches(isEnabled()))
    }
}

fun ensureConfigError() {
    log("ensureConfigError")
    tryOnUiUntilTimeout(25000, 1000) {
        onView(withId(R.id.alert_title))
            .check(matches(isDisplayed()))
            .check(matches(withText(R.string.configuration_error_title)))
    }
}
