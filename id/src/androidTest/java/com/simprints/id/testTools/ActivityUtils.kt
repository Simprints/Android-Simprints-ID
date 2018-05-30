package com.simprints.id.testTools

import android.Manifest
import android.content.Intent
import android.os.Build
import android.support.test.InstrumentationRegistry
import android.support.test.rule.ActivityTestRule
import android.view.WindowManager
import com.schibsted.spain.barista.permission.PermissionGranter
import com.simprints.id.activities.checkLogin.openedByIntent.CheckLoginFromIntentActivity
import com.simprints.libsimprints.Constants
import java.util.*

object ActivityUtils {

    private val permissions = ArrayList(Arrays.asList(
        Manifest.permission.ACCESS_NETWORK_STATE,
        Manifest.permission.BLUETOOTH,
        Manifest.permission.INTERNET,
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.RECEIVE_BOOT_COMPLETED,
        Manifest.permission.WAKE_LOCK,
        Manifest.permission.VIBRATE
    ))

    fun launchActivityAndRunOnUiThread(calloutCredentials: CalloutCredentials,
                                       action: String,
                                       activityTestRule: ActivityTestRule<*>,
                                       verifyGuidExtra: String? = null) {
        val intent = createIntent(calloutCredentials, action)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        if (verifyGuidExtra != null) intent.putExtra(Constants.SIMPRINTS_VERIFY_GUID, verifyGuidExtra)
        activityTestRule.launchActivity(intent)
        runActivityOnUiThread(activityTestRule)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) grantPermissions()
    }

    private fun createIntent(calloutCredentials: CalloutCredentials, action: String): Intent {
        val targetContext = InstrumentationRegistry.getInstrumentation().targetContext
        val intent = Intent(targetContext, CheckLoginFromIntentActivity::class.java)
        intent.action = action
        if (calloutCredentials.projectId.isNotEmpty())
            intent.putExtra(Constants.SIMPRINTS_PROJECT_ID, calloutCredentials.projectId)
        else
            intent.putExtra(Constants.SIMPRINTS_API_KEY, calloutCredentials.legacyApiKey)
        intent.putExtra(Constants.SIMPRINTS_USER_ID, calloutCredentials.userId)
        intent.putExtra(Constants.SIMPRINTS_MODULE_ID, calloutCredentials.moduleId)
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
}
