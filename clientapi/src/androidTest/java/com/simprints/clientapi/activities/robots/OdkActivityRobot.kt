package com.simprints.clientapi.activities.robots

import androidx.test.rule.ActivityTestRule
import com.simprints.clientapi.activities.odk.OdkActivity
import com.simprints.clientapi.activities.odk.OdkActivityTest
import com.simprints.testtools.android.BaseAssertions
import com.simprints.testtools.common.syntax.verifyOnce

fun OdkActivityTest.odk(func: OdkActivityRobot.() -> Unit) = OdkActivityRobot(rule).apply(func)

class OdkActivityRobot(private val rule: ActivityTestRule<OdkActivity>) {

    infix fun assert(func: OdkActivityAssertions.() -> Unit) {
        OdkActivityAssertions(rule).run(func)
    }

}

class OdkActivityAssertions(private val rule: ActivityTestRule<OdkActivity>) : BaseAssertions() {

    fun toastMessageIsDisplayed() {
        verifyOnce(rule.activity.guidSelectionNotifier) {
            showMessage()
        }
    }

}
