package com.simprints.testtools.android

import android.app.Activity
import android.view.WindowManager
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.ActivityTestRule
import androidx.test.runner.lifecycle.ActivityLifecycleMonitorRegistry
import androidx.test.runner.lifecycle.Stage

fun runActivityOnUiThread(activityTestRule: ActivityTestRule<*>) {
    val activity = activityTestRule.activity
    val wakeUpDevice = Runnable {
        activity.window.addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
            WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }
    activity.runOnUiThread(wakeUpDevice)
}

fun getCurrentActivity(): Activity? {
    InstrumentationRegistry.getInstrumentation().let {
        it.waitForIdleSync()
        val activity = arrayOfNulls<Activity>(1)
        it.runOnMainSync {
            activity[0] = ActivityLifecycleMonitorRegistry.getInstance().getActivitiesInStage(Stage.RESUMED).firstOrNull()
        }
        return activity[0]
    }
}
