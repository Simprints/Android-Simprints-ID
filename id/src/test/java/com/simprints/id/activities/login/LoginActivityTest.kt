package com.simprints.id.activities.login

import android.app.Activity
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.ApplicationInfo
import android.content.pm.ResolveInfo
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.simprints.id.R
import com.simprints.id.activities.login.request.LoginActivityRequest
import com.simprints.id.commontesttools.DefaultTestConstants.DEFAULT_PROJECT_ID
import com.simprints.id.commontesttools.DefaultTestConstants.DEFAULT_PROJECT_SECRET
import com.simprints.id.commontesttools.DefaultTestConstants.DEFAULT_USER_ID
import com.simprints.id.commontesttools.di.TestAppModule
import com.simprints.id.secure.ProjectAuthenticator
import com.simprints.id.testtools.TestApplication
import com.simprints.id.testtools.UnitTestConfig
import com.simprints.id.tools.extensions.scannerAppIntent
import com.simprints.testtools.common.di.DependencyRule.MockkRule
import com.simprints.testtools.unit.robolectric.ShadowAndroidXMultiDex
import com.simprints.testtools.unit.robolectric.createActivity
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.reactivex.Completable
import kotlinx.android.synthetic.main.activity_login.*
import org.junit.Assert
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowToast

@RunWith(AndroidJUnit4::class)
@Config(application = TestApplication::class, shadows = [ShadowAndroidXMultiDex::class])
class LoginActivityTest {

    private val app = ApplicationProvider.getApplicationContext() as TestApplication

    private val module by lazy {
        TestAppModule(app,
            dbManagerRule = MockkRule,
            sessionEventsLocalDbManagerRule = MockkRule,
            crashReportManagerRule = MockkRule)
    }

    @Before
    fun setUp() {
        UnitTestConfig(this, module).fullSetup()
    }

    @Test
    fun shouldUserIdPreFilled() {

        val controller = createRoboLoginActivity(getIntentForLoginAct()).start().resume().visible()
        val activity = controller.get()
        val userIdInEditText = activity.loginEditTextUserId.text.toString()
        assertEquals(userIdInEditText, DEFAULT_USER_ID)
    }

    @Test
    fun loginSuccesses_shouldReturnSuccessResultCode() {
        val controller = createRoboLoginActivity(getIntentForLoginAct()).start().resume().visible()
        val projectAuthenticator = mockk<ProjectAuthenticator>()
        every { projectAuthenticator.authenticate(any(), any()) } returns Completable.complete()

        val loginAct = controller.get().apply {
            viewPresenter.projectAuthenticator = projectAuthenticator
            loginEditTextUserId.setText(DEFAULT_USER_ID)
            loginEditTextProjectId.setText(DEFAULT_PROJECT_ID)
            loginEditTextProjectSecret.setText(DEFAULT_PROJECT_SECRET)
            loginButtonSignIn.performClick()
        }

        val shadowLoginAct = shadowOf(loginAct)

        assertEquals(1, shadowLoginAct.resultCode)
        assertTrue(loginAct.isFinishing)
    }

    @Test
    fun qrScanPressedAndScannerAppNotAvailable_shouldOpenPlayStore() {

        val controller = createRoboLoginActivity(getIntentForLoginAct()).start().resume().visible()
        val activity = controller.get()

        activity.loginButtonScanQr.performClick()

        val nextActivity = shadowOf(activity).nextStartedActivity

        assertNotNull(nextActivity)

        val isIntentForGooglePlay: Boolean = (nextActivity.dataString
            ?: "").contains("play.google.com")
        assertTrue(isIntentForGooglePlay)
    }

    @Test
    fun qrScanPressedAndScannerAppIsAvailable_shouldOpenScannerApp() {

        val app = ApplicationProvider.getApplicationContext() as TestApplication
        val pm = app.packageManager

        val controller = createRoboLoginActivity(getIntentForLoginAct())
        val activity = controller.get()

        val spm = shadowOf(pm)
        spm.addResolveInfoForIntent(pm.scannerAppIntent(), injectHowToResolveScannerAppIntent())

        controller.start().resume().visible()
        activity.loginButtonScanQr.performClick()

        val nextActivity = shadowOf(activity).nextStartedActivity
        assertNotNull(nextActivity)

        val isIntentForScannerApp = nextActivity.action == "com.google.zxing.client.android.SCAN"
        Assert.assertTrue(isIntentForScannerApp)
    }

    @Test
    fun invalidScannedText_shouldOpenErrorAlert() {
        val controller = createRoboLoginActivity(getIntentForLoginAct())
        controller.start().resume().visible()
        val act = controller.get()
        act.handleScannerAppResult(Activity.RESULT_OK, Intent().putExtra("SCAN_RESULT", "{\"projectId\":\"someProjectId\",\"projectSecretWrong\":\"someSecret\"}"))

        assertEquals(app.getString(R.string.login_invalid_qr_code), ShadowToast.getTextOfLatestToast())
    }

    @Test
    fun validScannedText_shouldHaveProjectIdAndSecretInEditTexts() {
        val controller = createRoboLoginActivity(getIntentForLoginAct()).start().resume().visible()
        val act = controller.get()
        assertTrue(act.loginEditTextProjectId.text!!.isEmpty())
        assertTrue(act.loginEditTextProjectSecret.text!!.isEmpty())

        val projectId = "55KAiL2YmsjeuNNPnSDO"
        val projectSecret = "GMoqI_4-UToujbPrIHrNMS9_0EpCbXveTLCvvN7nasVDCNcyhuu7c8u2zrfkuVdL7t3Uxt-Rjo8sDvBi3bkpUA"

        act.handleScannerAppResult(Activity.RESULT_OK, Intent().putExtra("SCAN_RESULT", "{\"projectId\":\"$projectId\",\"projectSecret\":\"$projectSecret\"}"))

        assertEquals(projectId, act.loginEditTextProjectId.text.toString())
        assertEquals(projectSecret, act.loginEditTextProjectSecret.text.toString())
    }

    @Test
    fun loginPressed_shouldLoginInOnlyWithValidCredentials() {
        val controller = createRoboLoginActivity(getIntentForLoginAct()).start().resume().visible()
        val act = controller.get()
        act.loginEditTextUserId.setText("")
        act.loginEditTextProjectId.setText("")
        act.loginEditTextProjectSecret.setText("")

        act.loginButtonSignIn.performClick()
        assertEquals(app.getString(R.string.login_missing_credentials), ShadowToast.getTextOfLatestToast())

        act.loginEditTextProjectSecret.setText(DEFAULT_PROJECT_SECRET)
        act.loginButtonSignIn.performClick()
        assertEquals(app.getString(R.string.login_missing_credentials), ShadowToast.getTextOfLatestToast())

        act.loginEditTextProjectId.setText(DEFAULT_PROJECT_ID)
        act.loginButtonSignIn.performClick()
        assertEquals(app.getString(R.string.login_missing_credentials), ShadowToast.getTextOfLatestToast())

        act.viewPresenter = mockk<LoginPresenter>(relaxed = true)

        act.loginEditTextUserId.setText(DEFAULT_USER_ID)
        act.loginButtonSignIn.performClick()
        verify(atLeast = 1) {
            act.viewPresenter.signIn(DEFAULT_USER_ID,
                DEFAULT_PROJECT_ID,
                DEFAULT_PROJECT_SECRET,
                DEFAULT_PROJECT_ID)
        }
    }

    private fun getIntentForLoginAct() = Intent()
        .apply { putExtra(LoginActivityRequest.BUNDLE_KEY, LoginActivityRequest(DEFAULT_PROJECT_ID, DEFAULT_USER_ID)) }

    private fun createRoboLoginActivity(intent: Intent) =
        createActivity<LoginActivity>(intent)

    private fun injectHowToResolveScannerAppIntent(): ResolveInfo {
        // Pretend that ScannerQR app is installed
        val info = ResolveInfo()
        info.isDefault = true
        val applicationInfo = ApplicationInfo()
        applicationInfo.packageName = "com.google.zxing.client.android"
        applicationInfo.className = "com.google.zxing.client.android.CaptureActivity"
        info.activityInfo = ActivityInfo()
        info.activityInfo.applicationInfo = applicationInfo
        info.activityInfo.name = "Barcode Scanner"
        return info
    }
}
