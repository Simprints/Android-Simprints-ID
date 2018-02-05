package com.simprints.id.activities

import com.google.firebase.FirebaseApp
import com.simprints.id.Application
import com.simprints.id.BuildConfig
import com.simprints.id.activities.requestProjectCredentials.RequestProjectCredentialsActivity
import com.simprints.id.testUtils.assertActivityStarted
import com.simprints.id.tools.extensions.scannerAppIntent
import com.simprints.id.tools.roboletric.createRoboFrontViewActivity
import com.simprints.id.tools.roboletric.createRoboRequestProjectCredentialsActivity
import com.simprints.id.tools.roboletric.getRoboSharedPreferences
import com.simprints.id.tools.roboletric.injectHowToResolveScannerAppIntent
import kotlinx.android.synthetic.main.activity_request_project_secret.*
import org.junit.Assert
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config


@RunWith(RobolectricTestRunner::class)
@Config(constants = BuildConfig::class)
class FrontActivityTest {

    @Test
    @Throws(Exception::class)
    fun notProjectSecretStored_shouldBringRegistrationActivityUp() {

        FirebaseApp.initializeApp(RuntimeEnvironment.application)
        val controller = createRoboFrontViewActivity().start().resume().visible()
        val activity = controller.get()
        assertActivityStarted(RequestProjectCredentialsActivity::class.java, activity)
    }

    @Test
    @Throws(Exception::class)
    fun validProjectCredentialsStored_shouldStayOnFrontActivity() {

        FirebaseApp.initializeApp(RuntimeEnvironment.application)

        val sharedPreferences = getRoboSharedPreferences()
        sharedPreferences.edit().putString("ENCRYPTED_PROJECT_SECRET", "secret").commit()
        sharedPreferences.edit().putString("PROJECT_ID", "id").commit()

        val controller = createRoboFrontViewActivity().start().resume().visible()
        val activity = controller.get()

        assertNull(shadowOf(activity).nextStartedActivity)
    }

    @Test
    @Throws(Exception::class)
    fun qrScanPressedAndScannerAppNotAvailable_shouldOpenPlayStore() {

        FirebaseApp.initializeApp(RuntimeEnvironment.application)

        val controller = createRoboRequestProjectCredentialsActivity().start().resume().visible()
        val activity = controller.get()

        activity.requestProjectCredentialsButtonScanQr.performClick()

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

        val controller = createRoboRequestProjectCredentialsActivity()
        val activity = controller.get()

        val spm = shadowOf(pm)
        spm.addResolveInfoForIntent(pm.scannerAppIntent(), injectHowToResolveScannerAppIntent(pm))

        controller.start().resume().visible()
        activity.requestProjectCredentialsButtonScanQr.performClick()

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
