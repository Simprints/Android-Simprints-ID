package com.simprints.id.tools

import android.content.Intent
import android.support.test.InstrumentationRegistry
import android.support.test.rule.ActivityTestRule
import android.view.WindowManager
import com.simprints.id.activities.LaunchActivity
import com.simprints.libsimprints.Constants

object ActivityUtils {

    fun launchActivityAndRunOnUiThread(calloutCredentials: CalloutCredentials,
                                       action: String,
                                       activityTestRule: ActivityTestRule<*>,
                                       verifyGuidExtra: String? = null) {
        val intent = createLaunchActivityIntent(calloutCredentials, action)
        if (verifyGuidExtra != null) intent.putExtra(Constants.SIMPRINTS_VERIFY_GUID, verifyGuidExtra)
        activityTestRule.launchActivity(intent)
        runActivityOnUiThread(activityTestRule)
    }

    private fun createLaunchActivityIntent(calloutCredentials: CalloutCredentials, action: String): Intent {
        val targetContext = InstrumentationRegistry.getInstrumentation().targetContext
        val intent = Intent(targetContext, LaunchActivity::class.java)
        intent.action = action
        intent.putExtra(Constants.SIMPRINTS_API_KEY, calloutCredentials.apiKey)
        intent.putExtra(Constants.SIMPRINTS_USER_ID, calloutCredentials.userId)
        intent.putExtra(Constants.SIMPRINTS_MODULE_ID, calloutCredentials.moduleId)
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
}
