package com.simprints.id.testSnippets

import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.action.ViewActions.*
import android.support.test.espresso.assertion.ViewAssertions.matches
import android.support.test.espresso.matcher.ViewMatchers.*
import android.support.test.rule.ActivityTestRule
import com.simprints.id.R
import com.simprints.id.activities.checkLogin.openedByIntent.CheckLoginFromIntentActivity
import com.simprints.id.testTools.ActivityUtils
import com.simprints.id.testTools.models.TestCalloutCredentials
import com.simprints.id.testTools.log
import com.simprints.id.testTools.tryOnUiUntilTimeout
import com.simprints.libsimprints.Constants

fun launchAppFromIntentEnrol(testCalloutCredentials: TestCalloutCredentials,
                             enrolTestRule: ActivityTestRule<CheckLoginFromIntentActivity>) {
    log("launchAppFromIntentEnrol")
    ActivityUtils.launchActivityAndRunOnUiThread(testCalloutCredentials,
        Constants.SIMPRINTS_REGISTER_INTENT, enrolTestRule)
}

fun launchAppFromIntentEnrolAndDoLogin(testCalloutCredentials: TestCalloutCredentials,
                                       loginTestRule: ActivityTestRule<CheckLoginFromIntentActivity>,
                                       projectSecret: String) {
    launchAppFromIntentEnrol(testCalloutCredentials, loginTestRule)
    enterCredentialsDirectly(testCalloutCredentials, projectSecret)
    pressSignIn()
    ensureSignInSuccess()
}

fun enterCredentialsDirectly(testCalloutCredentials: TestCalloutCredentials, projectSecret: String) {
    log("enterCredentialsDirectly")
    tryOnUiUntilTimeout(10000, 50) {
        onView(withId(R.id.loginEditTextProjectId))
            .check(matches(isDisplayed()))
            .perform(clearText())
            .perform(typeText(testCalloutCredentials.projectId))
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
