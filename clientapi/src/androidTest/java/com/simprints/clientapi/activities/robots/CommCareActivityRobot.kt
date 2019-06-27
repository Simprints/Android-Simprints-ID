package com.simprints.clientapi.activities.robots

import com.simprints.clientapi.R

fun commCare(func: CommCareActivityRobot.() -> Unit) = CommCareActivityRobot().apply(func)

class CommCareActivityRobot {

    infix fun assert(func: CommCareActivityAssertions.() -> Unit) {
        CommCareActivityAssertions().run(func)
    }

}

class CommCareActivityAssertions : BaseAssertions() {

    fun dataSentIsDisplayedOnToast() {
        assertToastMessageIs(R.string.data_sent)
    }

}
