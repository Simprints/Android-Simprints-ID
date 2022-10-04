package com.simprints.id.activities.login

import android.app.Activity
import android.app.Instrumentation
import android.content.Intent
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.pressBackUnconditionally
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intending
import androidx.test.espresso.intent.matcher.IntentMatchers
import androidx.test.espresso.intent.matcher.IntentMatchers.isInternal
import androidx.test.espresso.matcher.RootMatchers.withDecorView
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.platform.app.InstrumentationRegistry
import com.adevinta.android.barista.assertion.BaristaVisibilityAssertions.assertDisplayed
import com.adevinta.android.barista.interaction.BaristaClickInteractions.clickOn
import com.adevinta.android.barista.interaction.BaristaEditTextInteractions.writeTo
import com.google.common.truth.Truth.assertThat
import com.simprints.id.R
import com.simprints.id.activities.alert.AlertActivity
import com.simprints.id.activities.login.request.LoginActivityRequest
import com.simprints.id.activities.login.response.LoginActivityResponse
import com.simprints.id.activities.login.response.QrCodeResponse
import com.simprints.id.activities.login.tools.LoginActivityHelper
import com.simprints.id.activities.qrcapture.QrCaptureActivity
import com.simprints.id.domain.moduleapi.app.responses.AppErrorResponse
import com.simprints.testtools.android.getCurrentActivity
import io.mockk.every
import org.hamcrest.CoreMatchers.not


const val USER_ID = "user_id"
const val VALID_PROJECT_ID = "project_id"
const val VALID_PROJECT_SECRET = "encrypted_project_secret"
const val SYNC_CARD_FAILED_BACKEND_MAINTENANCE_STATE_TIMED_MESSAGE =
    "The system is currently offline for maintenance. Please try again after 10 minutes, 00 seconds"
const val SYNC_CARD_FAILED_BACKEND_MAINTENANCE_STATE_MESSAGE =
    "The system is currently offline for maintenance. Please try again later."

private const val EXTRA_SCAN_RESULT = "SCAN_RESULT"

fun LoginActivityAndroidTest.loginActivity(
    block: LoginActivityRobot.() -> Unit
): LoginActivityRobot {
    val request = LoginActivityRequest(VALID_PROJECT_ID, USER_ID)
    val context = InstrumentationRegistry.getInstrumentation().targetContext

    val intent = Intent(context, LoginActivity::class.java)
        .putExtra(LoginActivityRequest.BUNDLE_KEY, request)

    InstrumentationRegistry.getInstrumentation().waitForIdleSync()
    val activityScenario = ActivityScenario.launch<LoginActivity>(intent)

    return LoginActivityRobot(
        activityScenario,
        mockLoginActivityHelper
    ).apply(block)
}

class LoginActivityRobot(
    private val activityScenario: ActivityScenario<LoginActivity>,
    private val mockLoginActivityHelper: LoginActivityHelper
) {

    fun typeProjectId(projectId: String) {
        writeTo(R.id.loginEditTextProjectId,projectId)
    }

    fun typeProjectSecret(projectSecret: String) {
        writeTo(R.id.loginEditTextProjectSecret,projectSecret)
    }

    fun withMandatoryCredentialsPresent() {
        shouldHaveMandatoryCredentials(true)
    }

    fun withMandatoryCredentialsMissing() {
        shouldHaveMandatoryCredentials(false)
    }

    fun withSuppliedProjectIdAndIntentProjectIdMatching() {
        shouldMatchSuppliedProjectIdAndIntentProjectId(true)
    }

    fun withSuppliedProjectIdAndIntentProjectIdNotMatching() {
        shouldMatchSuppliedProjectIdAndIntentProjectId(false)
    }

    fun withSecurityStatusRunning() {
        every { mockLoginActivityHelper.isSecurityStatusRunning() } returns true
    }

    fun receiveValidQrCodeResponse() {
        every {
            mockLoginActivityHelper.tryParseQrCodeResponse(any())
        } returns QrCodeResponse(
            VALID_PROJECT_ID,
            VALID_PROJECT_SECRET
        )
        stubQrScanIntent()
    }

    fun receiveInvalidQrCodeResponse() {
        every { mockLoginActivityHelper.tryParseQrCodeResponse(any()) } throws Throwable()
        stubQrScanIntent()
    }

    fun receiveQrScanError() {
        intending(isInternal()).respondWith(
            Instrumentation.ActivityResult(
                Activity.RESULT_CANCELED,
                null
            )
        )
    }

    infix fun clickScanQr(assertion: LoginActivityAssertions.() -> Unit) {
        clickOn(R.id.loginButtonScanQr)
        assert(assertion)
    }

    infix fun clickSignIn(assertion: LoginActivityAssertions.() -> Unit) {
        clickOn(R.id.loginButtonSignIn)
        assert(assertion)
    }

    infix fun pressBack(assertion: LoginActivityAssertions.() -> Unit) {
        pressBackUnconditionally()

        assert(assertion)
    }

    infix fun assert(assertion: LoginActivityAssertions.() -> Unit) {
        LoginActivityAssertions(activityScenario).apply(assertion)
    }

    private fun shouldHaveMandatoryCredentials(result: Boolean) {
        every {
            mockLoginActivityHelper.areMandatoryCredentialsPresent(any(), any(), any())
        } returns result
    }

    private fun shouldMatchSuppliedProjectIdAndIntentProjectId(result: Boolean) {
        every {
            mockLoginActivityHelper.areSuppliedProjectIdAndProjectIdFromIntentEqual(any(), any())
        } returns result
    }

    private fun stubQrScanIntent() {
        intending(isInternal()).respondWith(
            Instrumentation.ActivityResult(
                Activity.RESULT_OK,
                Intent().putExtra(EXTRA_SCAN_RESULT, "mock_qr_code")
            )
        )
    }
}

class LoginActivityAssertions(
    private val activityScenario: ActivityScenario<LoginActivity>
) {

    fun userIdFieldHasText(text: String) {
        assertDisplayed(R.id.loginEditTextUserId,text)
    }

    fun missingCredentialsToastIsDisplayed() {
        assertToastIsDisplayed(
            "Missing credentials. Please check the User ID, Project ID, and key, and try again."
        )
    }

    fun projectIdMismatchToastIsDisplayed() {
        assertToastIsDisplayed(
            "Project ID different from that supplied in intent. Please contact your system administrator."
        )
    }

       fun userIsSignedIn() {
        val result = activityScenario.result
        assertThat(result.resultCode).isEqualTo(LoginActivityResponse.RESULT_CODE_LOGIN_SUCCEED)
    }

    fun invalidCredentialsToastIsDisplayed() {
        assertToastIsDisplayed("Invalid credentials. Please check the Project ID and key")
    }

    fun offlineToastIsDisplayed() {
        assertToastIsDisplayed("Currently offline. Please check your internet connection.")
    }

    fun serverErrorToastIsDisplayed() {
        assertToastIsDisplayed(
            "A problem occurred trying to contact the server. Please try again later."
        )
    }

    fun alertScreenIsLaunched() {
        Intents.intended(IntentMatchers.hasComponent(AlertActivity::class.java.name))
    }

    fun qrCaptureActivityIsOpened() {
        Intents.intended(IntentMatchers.hasComponent(QrCaptureActivity::class.java.name))
    }

    fun invalidQrCodeToastIsDisplayed() {
        assertToastIsDisplayed("The scanned QR code is invalid.")
    }

    fun qrCodeErrorToastIsDisplayed() {
        assertToastIsDisplayed("A problem occurred while scanning the QR code")
    }

    fun projectIdFieldHasText(text: String) {
        assertDisplayed(R.id.loginEditTextProjectId,text)
    }

    fun projectSecretFieldHasText(text: String) {
        assertDisplayed(R.id.loginEditTextProjectSecret,text)
    }

    fun loginNotCompleteIntentIsReturned() {
        val result = activityScenario.result
        assertThat(result.resultCode).isEqualTo(Activity.RESULT_OK)

        result.resultData.setExtrasClassLoader(AppErrorResponse::class.java.classLoader)
        val response =
            result.resultData.getParcelableExtra<AppErrorResponse>(LoginActivityResponse.BUNDLE_KEY)

        assertThat(response?.reason).isEqualTo(AppErrorResponse.Reason.LOGIN_NOT_COMPLETE)
    }

    private fun assertToastIsDisplayed(message: String) {
        onView(withText(message))
            .inRoot(withDecorView(not((getCurrentActivity()?.window?.decorView))))
            .check(matches(isDisplayed()))
    }
}
