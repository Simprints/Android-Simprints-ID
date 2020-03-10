package com.simprints.clientapi.activities.robots

import androidx.test.rule.ActivityTestRule
import com.simprints.clientapi.activities.commcare.CommCareActivity
import com.simprints.clientapi.activities.commcare.CommCareActivityTest
import io.mockk.verify

fun CommCareActivityTest.commCare(func: CommCareActivityRobot.() -> Unit): CommCareActivityRobot {
    return CommCareActivityRobot(rule).apply(func)
}

class CommCareActivityRobot(private val rule: ActivityTestRule<CommCareActivity>) {

    infix fun assert(func: CommCareActivityAssertions.() -> Unit) {
        CommCareActivityAssertions(rule).run(func)
    }
}

class CommCareActivityAssertions(
    private val rule: ActivityTestRule<CommCareActivity>) {

    fun toastIsDisplayed() {
        verify(exactly = 1) { rule.activity.guidSelectionNotifier.showMessage() }
    }

}
