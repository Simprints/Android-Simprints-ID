package com.simprints.id.activities.login

import android.content.Intent
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.rule.ActivityTestRule
import com.google.common.truth.Truth
import com.simprints.id.R
import com.simprints.id.activities.checkLogin.openedByIntent.CheckLoginFromIntentActivity
import com.simprints.id.activities.login.request.LoginActivityRequest
import com.simprints.id.commontesttools.models.TestCalloutCredentials
import com.simprints.id.testtools.grantPermissions
import com.simprints.id.testtools.launchActivityAndRunOnUiThread
import com.simprints.moduleapi.app.requests.IAppEnrollRequest
import com.simprints.moduleapi.app.requests.IAppRequest
import com.simprints.testtools.android.log
import com.simprints.testtools.android.tryOnUiUntilTimeout
import kotlinx.android.parcel.Parcelize

@Parcelize
private data class AppEnrollRequest(
    override val projectId: String,
    override val userId: String,
    override val moduleId: String,
    override val metadata: String
) : IAppEnrollRequest

fun launchCheckLoginActivityEnrol(testCalloutCredentials: TestCalloutCredentials,
                        enrolTestRule: ActivityTestRule<CheckLoginFromIntentActivity>) {
    log("launchAppFromIntentEnrol")
    val intent = Intent().apply {
        putExtra(IAppRequest.BUNDLE_KEY, AppEnrollRequest(
            testCalloutCredentials.projectId,
            testCalloutCredentials.userId,
            testCalloutCredentials.moduleId,
            ""))
    }
    enrolTestRule.launchActivityAndRunOnUiThread(intent)
}

fun launchLoginActivity(testCalloutCredentials: TestCalloutCredentials,
                        enrolTestRule: ActivityTestRule<LoginActivity>) {
    log("launchAppFromIntentEnrol")
    val intent = Intent().apply {
        putExtra(LoginActivityRequest.BUNDLE_KEY, LoginActivityRequest(
            testCalloutCredentials.projectId,
            testCalloutCredentials.userId))
    }
    enrolTestRule.launchActivityAndRunOnUiThread(intent)
}

fun enterCredentialsDirectly(testCalloutCredentials: TestCalloutCredentials, projectSecret: String) {
    log("enterCredentialsDirectly")
    tryOnUiUntilTimeout(20000, 50) {
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

fun ensureSignInSuccess(loginActivityTestRule: ActivityTestRule<*>) {
    log("ensureSignInSuccess")
    tryOnUiUntilTimeout(25000, 50) {
        Truth.assertThat(loginActivityTestRule.activity.isFinishing).isTrue()
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
