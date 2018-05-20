package com.simprints.id.activities

import android.app.Activity
import android.content.Intent
import com.google.firebase.FirebaseApp
import com.simprints.id.Application
import com.simprints.id.BuildConfig
import com.simprints.id.R
import com.simprints.id.activities.login.LoginPresenter
import com.simprints.id.secure.LegacyCompatibleProjectAuthenticator
import shared.anyNotNull
import com.simprints.id.testUtils.base.RxJavaTest
import com.simprints.id.testUtils.roboletric.*
import shared.whenever
import com.simprints.id.tools.extensions.scannerAppIntent
import io.reactivex.Completable
import kotlinx.android.synthetic.main.activity_login.*
import org.junit.Assert
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito
import org.mockito.Mockito.mock
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowToast

@RunWith(RobolectricTestRunner::class)
@Config(constants = BuildConfig::class, application = TestApplication::class)
class LoginActivityTest : RxJavaTest() {

    companion object {
        const val DEFAULT_PROJECT_ID = "some_project_id"
        const val DEFAULT_PROJECT_SECRET = "some_project_secret"
        const val DEFAULT_USER_ID = "some_user_id"
    }

    private lateinit var app: Application

    @Before
    fun setUp() {
        FirebaseApp.initializeApp(RuntimeEnvironment.application)
        app = (RuntimeEnvironment.application as Application)
        createMockForLocalDbManager(app)
        createMockForDbManager(app)
    }

    @Test
    fun shouldUserIdPreFilled() {
        val userId = "some_user_id"
        app.dataManager.userId = userId

        val controller = createRoboLoginActivity().start().resume().visible()
        val activity = controller.get()
        val userIdInEditText = activity.loginEditTextUserId.text.toString()
        assertEquals(userIdInEditText, userId)
    }

    @Test
    fun loginSuccesses_shouldReturnSuccessResultCode() {

        val controller = createRoboLoginActivity().start().resume().visible()
        val projectAuthenticator = mock(LegacyCompatibleProjectAuthenticator::class.java)
        whenever(projectAuthenticator.authenticate(anyNotNull(), anyNotNull(), anyNotNull(), any())).thenReturn(Completable.complete())

        val loginAct = controller.get().apply {
            viewPresenter.projectAuthenticator = projectAuthenticator
            loginEditTextUserId.setText("some_user_id")
            loginEditTextProjectId.setText("some_project_id")
            loginEditTextProjectSecret.setText("some_project_secret")
            loginButtonSignIn.performClick()
        }

        val shadowLoginAct = shadowOf(loginAct)

        assertEquals(1, shadowLoginAct.resultCode)
        assertTrue(shadowLoginAct.isFinishing)
    }

    @Test
    fun qrScanPressedAndScannerAppNotAvailable_shouldOpenPlayStore() {

        val controller = createRoboLoginActivity().start().resume().visible()
        val activity = controller.get()

        activity.loginButtonScanQr.performClick()

        val nextActivity = shadowOf(activity).nextStartedActivity

        assertNotNull(nextActivity)

        val isIntentForGooglePlay: Boolean = nextActivity.dataString.contains("play.google.com")
        assertTrue(isIntentForGooglePlay)
    }

    @Test
    fun qrScanPressedAndScannerAppIsAvailable_shouldOpenScannerApp() {

        val app = RuntimeEnvironment.application as Application
        FirebaseApp.initializeApp(app)
        val pm = app.packageManager

        val controller = createRoboLoginActivity()
        val activity = controller.get()

        val spm = shadowOf(pm)
        spm.addResolveInfoForIntent(pm.scannerAppIntent(), injectHowToResolveScannerAppIntent(pm))

        controller.start().resume().visible()
        activity.loginButtonScanQr.performClick()

        val nextActivity = shadowOf(activity).nextStartedActivity
        assertNotNull(nextActivity)

        val isIntentForScannerApp = nextActivity.action == "com.google.zxing.client.android.SCAN"
        Assert.assertTrue(isIntentForScannerApp)
    }

    @Test
    fun invalidScannedText_shouldOpenErrorAlert() {
        val controller = createRoboLoginActivity()
        controller.start().resume().visible()
        val act = controller.get()
        act.handleScannerAppResult(Activity.RESULT_OK, Intent().putExtra("SCAN_RESULT", "{\"projectId\":\"someProjectId\",\"projectSecretWrong\":\"someSecret\"}"))

        assertEquals(app.getString(R.string.login_invalid_qr_code), ShadowToast.getTextOfLatestToast())
    }

    @Test
    fun validScannedText_shouldHaveProjectIdAndSecretInEditTexts() {
        val controller = createRoboLoginActivity().start().resume().visible()
        val act = controller.get()
        assertTrue(act.loginEditTextProjectId.text.isEmpty())
        assertTrue(act.loginEditTextProjectSecret.text.isEmpty())

        val projectId = "55KAiL2YmsjeuNNPnSDO"
        val projectSecret = "GMoqI_4-UToujbPrIHrNMS9_0EpCbXveTLCvvN7nasVDCNcyhuu7c8u2zrfkuVdL7t3Uxt-Rjo8sDvBi3bkpUA"

        act.handleScannerAppResult(Activity.RESULT_OK, Intent().putExtra("SCAN_RESULT", "{\"projectId\":\"$projectId\",\"projectSecret\":\"$projectSecret\"}"))

        assertEquals(projectId, act.loginEditTextProjectId.text.toString())
        assertEquals(projectSecret, act.loginEditTextProjectSecret.text.toString())
    }

    @Test
    fun loginPressed_shouldLoginInOnlyWithValidCredentials() {
        val controller = createRoboLoginActivity().start().resume().visible()
        val act = controller.get()
        act.loginEditTextUserId.setText("")
        act.loginEditTextProjectId.setText("")
        act.loginEditTextProjectSecret.setText("")

        act.loginButtonSignIn.performClick()
        assertEquals(app.getString(R.string.login_missing_credentials), ShadowToast.getTextOfLatestToast())

        act.loginEditTextProjectSecret.setText("some_project_secret")
        act.loginButtonSignIn.performClick()
        assertEquals(app.getString(R.string.login_missing_credentials), ShadowToast.getTextOfLatestToast())

        act.loginEditTextProjectId.setText("some_project_id")
        act.loginButtonSignIn.performClick()
        assertEquals(app.getString(R.string.login_missing_credentials), ShadowToast.getTextOfLatestToast())

        act.viewPresenter = mock(LoginPresenter::class.java)

        act.loginEditTextUserId.setText("some_user_id")
        act.loginButtonSignIn.performClick()
        Mockito.verify(act.viewPresenter, Mockito.times(1))
            .signIn(
                "some_user_id",
                "some_project_id",
                "some_project_secret",
                "")
    }

    @Test
    fun passedLegacyApiKey_shouldLoginInAndStoreIt() {
        val intent = Intent()
        intent.putExtra(IntentKeys.loginActivityLegacyProjectIdKey, "some_legacy_api_key")
        val controller = createRoboLoginActivity(intent).start().resume().visible()
        val act = controller.get()
        act.loginEditTextUserId.setText(DEFAULT_USER_ID)
        act.loginEditTextProjectId.setText(DEFAULT_PROJECT_ID)
        act.loginEditTextProjectSecret.setText(DEFAULT_PROJECT_SECRET)
        act.viewPresenter = mock(LoginPresenter::class.java)

        act.loginButtonSignIn.performClick()

        Mockito.verify(act.viewPresenter, Mockito.times(1))
            .signIn(
                DEFAULT_USER_ID,
                DEFAULT_PROJECT_ID,
                DEFAULT_PROJECT_SECRET,
                "",
                "some_legacy_api_key")
    }
}
