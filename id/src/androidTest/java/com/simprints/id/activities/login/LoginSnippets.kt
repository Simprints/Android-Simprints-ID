package com.simprints.id.activities.login

import android.content.Intent
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.replaceText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import com.google.common.truth.Truth
import com.simprints.id.Application
import com.simprints.id.R
import com.simprints.id.activities.login.request.LoginActivityRequest
import com.simprints.id.activities.login.response.LoginActivityResponse.Companion.RESULT_CODE_LOGIN_SUCCEED
import com.simprints.id.commontesttools.models.TestCalloutCredentials
import com.simprints.testtools.android.log

fun launchLoginActivity(testCalloutCredentials: TestCalloutCredentials) =
    ActivityScenario.launch<LoginActivity>(Intent().apply {
        setClassName(ApplicationProvider.getApplicationContext<Application>().packageName, LoginActivity::class.qualifiedName!!)
        putExtra(LoginActivityRequest.BUNDLE_KEY, LoginActivityRequest(
            testCalloutCredentials.projectId,
            testCalloutCredentials.userId))
    })

fun enterCredentialsDirectly(testCalloutCredentials: TestCalloutCredentials, projectSecret: String) {
    log("enterCredentialsDirectly")
    onView(withId(R.id.loginEditTextProjectId))
        .check(matches(isDisplayed()))
        .perform(replaceText(testCalloutCredentials.projectId))
    onView(withId(R.id.loginEditTextProjectSecret))
        .check(matches(isDisplayed()))
        .perform(replaceText(projectSecret))

}

fun pressSignIn() {
    log("pressSignIn")
    onView(withId(R.id.loginButtonSignIn))
        .check(matches(isDisplayed()))
        .check(matches(isClickable()))
        .perform(click())
}

fun ensureSignInSuccess(scenario: ActivityScenario<LoginActivity>) {
    val result = scenario.result
    Truth.assertThat(result.resultCode).isEqualTo(RESULT_CODE_LOGIN_SUCCEED)
}
