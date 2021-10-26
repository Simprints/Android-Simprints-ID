package com.simprints.testtools.android

import android.app.Activity
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.runner.lifecycle.ActivityLifecycleMonitorRegistry
import androidx.test.runner.lifecycle.Stage

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
