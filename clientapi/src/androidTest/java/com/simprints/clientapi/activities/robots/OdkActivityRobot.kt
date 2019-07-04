package com.simprints.clientapi.activities.robots

import com.simprints.clientapi.R
import com.simprints.testtools.android.BaseAssertions

fun odk(func: OdkActivityRobot.() -> Unit) = OdkActivityRobot().apply(func)

class OdkActivityRobot {

    infix fun assert(func: OdkActivityAssertions.() -> Unit) {
        OdkActivityAssertions().run(func)
    }

}

class OdkActivityAssertions : BaseAssertions() {

    fun resultSentIsDisplayedOnToast() {
        assertToastMessageIs(R.string.guid_selection_result_sent)
    }

}
