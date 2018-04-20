package com.simprints.id.tools

import android.support.test.rule.ActivityTestRule
import com.simprints.id.Application


object AppUtils {

    fun getApp(activityTestRule: ActivityTestRule<*>): Application =
        activityTestRule.activity.application as Application
}
