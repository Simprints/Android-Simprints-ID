package com.simprints.clientapi.activities.robots

import com.simprints.clientapi.R
import com.simprints.testtools.android.BaseAssertions

fun commCare(func: CommCareActivityRobot.() -> Unit) = CommCareActivityRobot().apply(func)

class CommCareActivityRobot {

    infix fun assert(func: CommCareActivityAssertions.() -> Unit) {
        CommCareActivityAssertions().run(func)
    }

}

class CommCareActivityAssertions : BaseAssertions() {

    fun dataSentIsDisplayedOnToast() {
        assertToastMessageIs(R.string.guid_selection_data_sent)
    }

}
