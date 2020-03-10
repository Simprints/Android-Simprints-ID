package com.simprints.id.activities.login

import android.app.Activity
import android.content.Intent
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.pressBackUnconditionally
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.RootMatchers.withDecorView
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withText
import br.com.concretesolutions.kappuccino.actions.ClickActions.click
import br.com.concretesolutions.kappuccino.actions.TextActions.typeText
import br.com.concretesolutions.kappuccino.assertions.VisibilityAssertions.displayed
import br.com.concretesolutions.kappuccino.custom.intent.IntentMatcherInteractions.sentIntent
import br.com.concretesolutions.kappuccino.custom.intent.IntentMatcherInteractions.stubIntent
import com.google.common.truth.Truth.assertThat
import com.simprints.core.tools.json.JsonHelper
import com.simprints.id.R
import com.simprints.id.activities.login.request.LoginActivityRequest
import com.simprints.id.activities.login.response.LoginActivityResponse
import com.simprints.id.data.analytics.crashreport.CrashReportManager
import com.simprints.id.data.analytics.crashreport.CrashReportTag
import com.simprints.id.data.analytics.crashreport.CrashReportTrigger
import com.simprints.id.domain.moduleapi.app.responses.AppErrorResponse
import com.simprints.testtools.android.getCurrentActivity
import io.mockk.verify
import org.hamcrest.CoreMatchers.not

const val USER_ID = "user_id"
const val VALID_PROJECT_ID = "project_id"
const val VALID_PROJECT_SECRET = "encrypted_project_secret"

private const val EXTRA_SCAN_RESULT = "SCAN_RESULT"

fun LoginActivityAndroidTest2.loginActivity(
    block: LoginActivityRobot.() -> Unit
): LoginActivityRobot {
    val request = LoginActivityRequest(VALID_PROJECT_ID, USER_ID)
    val intent = Intent().putExtra(LoginActivityRequest.BUNDLE_KEY, request)
    val activityScenario = ActivityScenario.launch<LoginActivity>(intent)

    return LoginActivityRobot(activityScenario, mockCrashReportManager).apply(block)
}

class LoginActivityRobot(
    private val activityScenario: ActivityScenario<LoginActivity>,
    private val mockCrashReportManager: CrashReportManager
) {

    fun typeProjectId(projectId: String) {
        typeText(projectId) {
            id(R.id.loginEditTextProjectId)
        }
    }

    fun typeProjectSecret(projectSecret: String) {
        typeText(projectSecret) {
            id(R.id.loginEditTextProjectSecret)
        }
    }

    infix fun clickScanQr(assertion: LoginActivityAssertions.() -> Unit) {
        click {
            id(R.id.loginButtonScanQr)
        }

        assert(assertion)
    }

    infix fun clickSignIn(assertion: LoginActivityAssertions.() -> Unit) {
        click {
            id(R.id.loginButtonSignIn)
        }

        assert(assertion)
    }

    infix fun pressBack(assertion: LoginActivityAssertions.() -> Unit) {
        pressBackUnconditionally()

        assert(assertion)
    }

    infix fun receiveValidQrCodeResponse(assertion: LoginActivityAssertions.() -> Unit) {
        stubIntent {
            respondWith {
                val response = JsonHelper.toJson(CredentialsResponse(VALID_PROJECT_ID, VALID_PROJECT_SECRET))
                val data = Intent().putExtra(EXTRA_SCAN_RESULT, response)
                ok()
                data(data)
            }
        }

        assert(assertion)
    }

    infix fun receiveInvalidQrCodeResponse(assertion: LoginActivityAssertions.() -> Unit) {
        stubIntent {
            respondWith {
                val data = Intent().putExtra(EXTRA_SCAN_RESULT, "invalid_json")
                ok()
                data(data)
            }
        }

        assert(assertion)
    }

    infix fun receiveErrorFromScannerApp(assertion: LoginActivityAssertions.() -> Unit) {
        stubIntent {
            respondWith {
                canceled()
            }
        }

        assert(assertion)
    }

    infix fun assert(assertion: LoginActivityAssertions.() -> Unit) {
        LoginActivityAssertions(activityScenario, mockCrashReportManager).apply(assertion)
    }

}

class LoginActivityAssertions(
    private val activityScenario: ActivityScenario<LoginActivity>,
    private val mockCrashReportManager: CrashReportManager
) {

    fun assertScannerAppIsLaunched() {
        sentIntent {
            action("com.google.zxing.client.android.SCAN")
        }
    }

    fun assertScannerAppPlayStorePageIsOpened() {
        sentIntent {
            action(Intent.ACTION_VIEW)
            url("https://play.google.com/store/apps/details?id=com.google.zxing.client.android")
        }
    }

    fun assertUserIsSignedIn() {
        val result = activityScenario.result
        assertThat(result.resultCode).isEqualTo(LoginActivityResponse.RESULT_CODE_LOGIN_SUCCEED)
    }

    fun assertInvalidCredentialsToastIsDisplayed() {
        assertToastIsDisplayed("Invalid credentials. Please check the Project ID and key")
    }

    fun assertProjectIdMismatchToastIsDisplayed() {
        assertToastIsDisplayed("Project ID different from that supplied in intent. Please contact your system administrator.")
    }

    fun assertInvalidProjectSecretToastIsDisplayed() {
        assertToastIsDisplayed("Project ID different from that supplied in intent. Please contact your system administrator.")
    }

    fun assertOfflineToastIsDisplayed() {
        assertToastIsDisplayed("Currently offline. Please check your internet connection.")
    }

    fun assertInvalidQrCodeToastIsDisplayed() {
        assertToastIsDisplayed("The scanned QR code is invalid.")
    }

    fun assertQrCodeErrorToastIsDisplayed() {
        assertToastIsDisplayed("A problem occurred while scanning the QR code")
    }

    fun assertServerErrorToastIsDisplayed() {
        assertToastIsDisplayed("A problem occurred trying to contact the server. Please try again later.")
    }

    fun assertUserIdFieldHasText(text: String) {
        displayed {
            allOf {
                id(R.id.loginEditTextUserId)
                text(text)
            }
        }
    }

    fun assertProjectIdFieldHasText(text: String) {
        displayed {
            allOf {
                id(R.id.loginEditTextProjectId)
                text(text)
            }
        }
    }

    fun assertProjectSecretFieldHasText(text: String) {
        displayed {
            allOf {
                id(R.id.loginEditTextProjectSecret)
                text(text)
            }
        }
    }

    fun assertLoginNotCompleteIntentIsReturned() {
        val result = activityScenario.result
        assertThat(result.resultCode).isEqualTo(Activity.RESULT_OK)

        result.resultData.setExtrasClassLoader(AppErrorResponse::class.java.classLoader)
        val response =
            result.resultData.getParcelableExtra<AppErrorResponse>(LoginActivityResponse.BUNDLE_KEY)

        assertThat(response.reason).isEqualTo(AppErrorResponse.Reason.LOGIN_NOT_COMPLETE)
    }

    fun assertMessageIsLoggedToCrashReport(message: String) {
        verify {
            mockCrashReportManager.logMessageForCrashReport(
                CrashReportTag.LOGIN,
                CrashReportTrigger.UI,
                message = message
            )
        }
    }

    private fun assertToastIsDisplayed(message: String) {
        onView(withText(message))
            .inRoot(withDecorView(not((getCurrentActivity()?.window?.decorView))))
            .check(matches(isDisplayed()))
    }

}
