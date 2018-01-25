package com.simprints.id.activities

import android.content.Context
import android.content.pm.ActivityInfo
import android.content.pm.ApplicationInfo
import android.content.pm.ResolveInfo
import com.google.firebase.FirebaseApp
import com.simprints.id.Application
import com.simprints.id.BuildConfig
import com.simprints.id.activities.front.FrontActivity
import com.simprints.id.activities.requestProjectKey.RequestProjectKeyActivity
import com.simprints.id.data.prefs.PreferencesManagerImpl
import com.simprints.id.testUtils.assertActivityStarted
import com.simprints.id.tools.extensions.scannerAppIntent
import kotlinx.android.synthetic.main.activity_request_project_key.*
import org.junit.Assert
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config


/**
 * Created by fabiotuzza on 25/01/2018.
 */
@RunWith(RobolectricTestRunner::class)
@Config(constants = BuildConfig::class)
class FrontActivityTest {

    @Test
    @Throws(Exception::class)
    fun notApiKeyStored_shouldBringRegistrationActivityUp() {

        FirebaseApp.initializeApp(RuntimeEnvironment.application);
        val controller = Robolectric.buildActivity(FrontActivity::class.java).create().start().resume().visible()
        val activity = controller.get()
        assertActivityStarted(RequestProjectKeyActivity::class.java, activity)
    }

    @Test
    @Throws(Exception::class)
    fun validApiKeyStored_shouldStayOnFrontActivity() {

        FirebaseApp.initializeApp(RuntimeEnvironment.application);

        val sharedPreferences = RuntimeEnvironment.application.getSharedPreferences(PreferencesManagerImpl.PREF_FILE_NAME, Context.MODE_PRIVATE)
        sharedPreferences.edit().putString("PROJECT_KEY", "some key").commit()

        val controller = Robolectric.buildActivity(FrontActivity::class.java).create().start().resume().visible()
        val activity = controller.get()

        assertNull(shadowOf(activity).nextStartedActivity)
    }

    @Test
    @Throws(Exception::class)
    fun qrScanPressed_shouldPlayStoreIfScannerNotAvailable() {

        FirebaseApp.initializeApp(RuntimeEnvironment.application);

        val controller = Robolectric.buildActivity(RequestProjectKeyActivity::class.java).create().start().resume().visible()
        val activity = controller.get()
        activity.requestProjectKeyButtonScanQr.performClick()

        assertNotNull(shadowOf(activity).nextStartedActivity)

        val isIntentForGooglePlay: Boolean = shadowOf(activity).nextStartedActivity.dataString.contains("play.google.com")
        assert(isIntentForGooglePlay)
    }


    @Test
    @Throws(Exception::class)
    fun qrScanPressed_shouldOpenScannerAppIfAvailable() {

        val app = RuntimeEnvironment.application as Application
        FirebaseApp.initializeApp(app)
        val packageManager = app.packageManager

        val controller = Robolectric.buildActivity(RequestProjectKeyActivity::class.java).create()
        val activity = controller.get()

        // Pretend that ScannerQR app is installed
        val spm = shadowOf(packageManager)
        val info = ResolveInfo()
        info.isDefault = true
        val applicationInfo = ApplicationInfo()
        applicationInfo.packageName = "com.google.zxing.client.android"
        applicationInfo.className = "com.google.zxing.client.android.CaptureActivity"
        info.activityInfo = ActivityInfo()
        info.activityInfo.applicationInfo = applicationInfo
        info.activityInfo.name = "Barcode Scanner"
        spm.addResolveInfoForIntent(packageManager.scannerAppIntent(), info)

        controller.start().resume().visible()
        activity.requestProjectKeyButtonScanQr.performClick()

        assertNotNull(shadowOf(activity).nextStartedActivity)

        val isIntentForGooglePlay: Boolean = shadowOf(activity).nextStartedActivity.dataString.contains("play.google.com")
        assertFalse(isIntentForGooglePlay)
    }

    @Test
    @Throws(Exception::class)
    fun enterButtonPressed_doSomething() {

        val controller = Robolectric.buildActivity(RequestProjectKeyActivity::class.java).create().start().resume().visible()
        val activity = controller.get()
        activity.requestProjectKeyEditTextProjectKey.setText("some key")
        activity.requestProjectKeyButtonEnterKey.performClick()

        assertTrue(activity.isFinishing)
        Assert.fail("Not implemented yet!")
    }
}
