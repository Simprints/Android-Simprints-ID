package com.simprints.id.activities

import android.app.Activity
import android.content.Intent
import com.google.firebase.FirebaseApp
import com.simprints.id.Application
import com.simprints.id.BuildConfig
import com.simprints.id.R
import com.simprints.id.activities.login.LoginPresenter
import com.simprints.id.secure.ProjectAuthenticator
import com.simprints.id.secure.models.Tokens
import com.simprints.id.testUtils.anyNotNull
import com.simprints.id.tools.extensions.scannerAppIntent
import com.simprints.id.tools.roboletric.createRoboLoginActivity
import com.simprints.id.tools.roboletric.injectHowToResolveScannerAppIntent
import com.simprints.id.tools.roboletric.mockLocalDbManager
import io.reactivex.internal.operators.single.SingleJust
import junit.framework.Assert.assertNotNull
import junit.framework.Assert.assertTrue
import kotlinx.android.synthetic.main.activity_login.*
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.mockito.Mockito.doReturn
import org.mockito.Mockito.mock
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowToast


@RunWith(RobolectricTestRunner::class)
@Config(constants = BuildConfig::class)
class LoginActivityTest {

    private lateinit var app: Application

    @Before
    fun setUp() {
        FirebaseApp.initializeApp(RuntimeEnvironment.application)
        app = (RuntimeEnvironment.application as Application)
        mockLocalDbManager(app)
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
        val projectAuthenticator = mock(ProjectAuthenticator::class.java)
        doReturn(SingleJust(Tokens("firestore_token", "legacy_token")))
            .`when`(projectAuthenticator).authenticate(anyNotNull(), anyNotNull())

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

        FirebaseApp.initializeApp(RuntimeEnvironment.application)

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
        assert(isIntentForScannerApp)
    }

    @Test
    fun invalidScannedText_shouldOpenErrorAlert() {
        val controller = createRoboLoginActivity()
        controller.start().resume().visible()
        val act = controller.get()
        act.handleScannerAppResult(Activity.RESULT_OK, Intent().putExtra("SCAN_RESULT", "not_valid_scanned_text"))

        assertEquals(app.getString(R.string.login_invalidQrCode), ShadowToast.getTextOfLatestToast())
    }

    @Test
    fun validScannedText_shouldHaveProjectIdAndSecretInEditTexts() {
        val controller = createRoboLoginActivity().start().resume().visible()
        val act = controller.get()
        assertTrue(act.loginEditTextProjectId.text.isEmpty())
        assertTrue(act.loginEditTextProjectSecret.text.isEmpty())

        act.handleScannerAppResult(Activity.RESULT_OK, Intent().putExtra("SCAN_RESULT", "id:some_project_id\nsecret:some_project_secret"))

        assertTrue(act.loginEditTextProjectId.text.isNotEmpty())
        assertTrue(act.loginEditTextProjectSecret.text.isNotEmpty())
    }

    @Test
    @Throws(Exception::class)
    fun enterButtonPressed_tryToLoginIn() {
        val controller = createRoboLoginActivity().start().resume().visible()
        val act = controller.get()
        act.viewPresenter = mock(LoginPresenter::class.java)
        act.loginEditTextUserId.setText("some_user_id")
        act.loginEditTextProjectId.setText("some_project_id")
        act.loginEditTextProjectSecret.setText("some_project_secret")

        act.loginButtonSignIn.performClick()

        Mockito.verify(act.viewPresenter, Mockito.times(1))
            .userDidWantToSignIn(
                "some_project_id",
                "some_project_secret",
                "some_user_id",
                "")
    }
}
