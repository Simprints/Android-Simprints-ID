package com.simprints.id.testTools

import android.support.test.rule.ActivityTestRule
import com.simprints.id.Application


object AppUtils {

    fun getApp(activityTestRule: ActivityTestRule<*>): Application =
        activityTestRule.activity.application as Application
}
