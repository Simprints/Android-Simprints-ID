package com.simprints.id.testTools

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.os.Build
import androidx.test.InstrumentationRegistry
import androidx.test.InstrumentationRegistry.getInstrumentation
import androidx.test.rule.ActivityTestRule
import android.view.WindowManager
import androidx.test.espresso.core.internal.deps.guava.collect.Iterables
import androidx.test.runner.lifecycle.ActivityLifecycleMonitorRegistry
import androidx.test.runner.lifecycle.Stage
import com.schibsted.spain.barista.interaction.PermissionGranter
import com.simprints.id.activities.checkLogin.openedByIntent.CheckLoginFromIntentActivity
import com.simprints.id.activities.collectFingerprints.CollectFingerprintsActivity
import com.simprints.id.shared.models.TestCalloutCredentials
import com.simprints.libsimprints.Constants
import java.util.*

object ActivityUtils {

    fun checkLoginFromIntentActivityTestRule() =
        ActivityTestRule(CheckLoginFromIntentActivity::class.java, false, false)

    private val permissions = ArrayList(Arrays.asList(
        Manifest.permission.ACCESS_NETWORK_STATE,
        Manifest.permission.BLUETOOTH,
        Manifest.permission.INTERNET,
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.RECEIVE_BOOT_COMPLETED,
        Manifest.permission.WAKE_LOCK,
        Manifest.permission.VIBRATE
    ))

    fun launchActivityAndRunOnUiThread(testCalloutCredentials: TestCalloutCredentials,
                                       action: String,
                                       activityTestRule: ActivityTestRule<*>,
                                       verifyGuidExtra: String? = null) {
        val intent = createIntent(testCalloutCredentials, action)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        if (verifyGuidExtra != null) intent.putExtra(Constants.SIMPRINTS_VERIFY_GUID, verifyGuidExtra)
        activityTestRule.launchActivity(intent)
        runActivityOnUiThread(activityTestRule)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) grantPermissions()
    }

    private fun createIntent(testCalloutCredentials: TestCalloutCredentials, action: String): Intent {
        val targetContext = InstrumentationRegistry.getInstrumentation().targetContext
        val intent = Intent(targetContext, CheckLoginFromIntentActivity::class.java)
        intent.action = action
        if (testCalloutCredentials.projectId.isNotEmpty())
            intent.putExtra(Constants.SIMPRINTS_PROJECT_ID, testCalloutCredentials.projectId)
        else intent.putExtra(Constants.SIMPRINTS_API_KEY, testCalloutCredentials.legacyApiKey)
        intent.putExtra(Constants.SIMPRINTS_USER_ID, testCalloutCredentials.userId)
        intent.putExtra(Constants.SIMPRINTS_MODULE_ID, testCalloutCredentials.moduleId)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        return intent
    }

    private fun runActivityOnUiThread(activityTestRule: ActivityTestRule<*>) {
        val activity = activityTestRule.activity
        val wakeUpDevice = Runnable {
            activity.window.addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
        activity.runOnUiThread(wakeUpDevice)
    }

    fun grantPermissions() {
        // Allow all first-app permissions and dismiss the dialog box
        log("ActivityUtils.grantPermissions(): granting permissions")
        for (permission in permissions) PermissionGranter.allowPermissionsIfNeeded(permission)
    }

    fun launchCollectFingerprintsActivity(collectFingerprintsTestRule: ActivityTestRule<CollectFingerprintsActivity>) {
        val intent = Intent()
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        collectFingerprintsTestRule.launchActivity(intent)
        runActivityOnUiThread(collectFingerprintsTestRule)
    }

    @Throws(Throwable::class)
    fun getCurrentActivity(): Activity? {
        getInstrumentation().let {
            it.waitForIdleSync()
            val activity = arrayOfNulls<Activity>(1)
            it.runOnMainSync {
                val activities = ActivityLifecycleMonitorRegistry.getInstance().getActivitiesInStage(Stage.RESUMED)
                activity[0] = Iterables.getOnlyElement(activities)
            }
            return activity[0]
        }
    }
}
