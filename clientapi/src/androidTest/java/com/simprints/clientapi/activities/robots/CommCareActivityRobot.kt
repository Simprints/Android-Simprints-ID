package com.simprints.clientapi.activities.robots

import androidx.test.rule.ActivityTestRule
import com.simprints.clientapi.activities.commcare.CommCareActivity
import com.simprints.clientapi.activities.commcare.CommCareActivityTest
import com.simprints.testtools.android.BaseAssertions
import com.simprints.testtools.common.syntax.verifyOnce

fun CommCareActivityTest.commCare(func: CommCareActivityRobot.() -> Unit): CommCareActivityRobot {
    return CommCareActivityRobot(rule).apply(func)
}

class CommCareActivityRobot(private val rule: ActivityTestRule<CommCareActivity>) {

    infix fun assert(func: CommCareActivityAssertions.() -> Unit) {
        CommCareActivityAssertions(rule).run(func)
    }

}

class CommCareActivityAssertions(
    private val rule: ActivityTestRule<CommCareActivity>
) : BaseAssertions() {

    fun toastIsDisplayed() {
        verifyOnce(rule.activity.guidSelectionNotifier) {
            showMessage()
        }
    }

}
