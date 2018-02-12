package com.simprints.id.activities

import com.google.firebase.FirebaseApp
import com.simprints.id.Application
import com.simprints.id.BuildConfig
import com.simprints.id.data.db.local.RealmDbManager
import com.simprints.id.secure.ProjectAuthenticator
import com.simprints.id.secure.models.Token
import com.simprints.id.testUtils.anyNotNull
import com.simprints.id.tools.extensions.scannerAppIntent
import com.simprints.id.tools.roboletric.createRoboLoginActivity
import com.simprints.id.tools.roboletric.injectHowToResolveScannerAppIntent
import io.reactivex.internal.operators.single.SingleJust
import junit.framework.Assert.*
import kotlinx.android.synthetic.main.activity_login.*
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.doReturn
import org.mockito.Mockito.mock
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(constants = BuildConfig::class)
class LoginActivityTest {

    private lateinit var app: Application

    @Before
    fun setUp() {
        FirebaseApp.initializeApp(RuntimeEnvironment.application)
        app = (RuntimeEnvironment.application as Application)
        app.localDbManager = mock(RealmDbManager::class.java)
    }

    @Test
    @Throws(Exception::class)
    fun shouldUserIdPreFilled() {
        val userId = "some_user_id"
        app.dataManager.userId = userId

        val controller = createRoboLoginActivity().start().resume().visible()
        val activity = controller.get()
        val userIdInEditText = activity.loginEditTextUserId.text.toString()
        assertEquals(userIdInEditText, userId)
    }

    @Test
    @Throws(Exception::class)
    fun loginSuccesses_shouldReturnSuccessResultCode() {

        val controller = createRoboLoginActivity().start().resume().visible()
        val loginAct = controller.get()
        val projectAuthenticator = mock(ProjectAuthenticator::class.java)
        doReturn(SingleJust(Token("token"))).`when`(projectAuthenticator).authenticateWithNewCredentials(anyNotNull(), anyNotNull(), anyNotNull())
        loginAct.viewPresenter.projectAuthenticator = projectAuthenticator
        loginAct.loginEditTextUserId.setText("some_user_id")
        loginAct.loginEditTextProjectId.setText("some_project_id")
        loginAct.loginEditTextProjectSecret.setText("some_project_secret")
        loginAct.loginButtonSignIn.performClick()

        val shadowLoginAct = shadowOf(loginAct)

        assertEquals(1, shadowLoginAct.resultCode)
        assertTrue(shadowLoginAct.isFinishing)
    }

    @Test
    @Throws(Exception::class)
    fun qrScanPressedAndScannerAppNotAvailable_shouldOpenPlayStore() {

        FirebaseApp.initializeApp(RuntimeEnvironment.application)

        val controller = createRoboLoginActivity().start().resume().visible()
        val activity = controller.get()

        activity.loginButtonScanQr.performClick()

        val nextActivity = shadowOf(activity).nextStartedActivity

        assertNotNull(nextActivity)

        val isIntentForGooglePlay: Boolean = nextActivity.dataString.contains("play.google.com")
        assert(isIntentForGooglePlay)
    }

    @Test
    @Throws(Exception::class)
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
    @Throws(Exception::class)
    fun enterButtonPressed_doSomething() {

        Assert.fail("Not implemented yet!")
    }
}
