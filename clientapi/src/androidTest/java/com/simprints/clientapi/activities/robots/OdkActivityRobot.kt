package com.simprints.clientapi.activities.robots

import androidx.test.rule.ActivityTestRule
import com.google.common.truth.Truth.assertThat
import com.simprints.clientapi.activities.odk.OdkActivity
import com.simprints.clientapi.activities.odk.OdkActivityTest
import com.simprints.clientapi.clientrequests.extractors.odk.OdkEnrolExtractor
import com.simprints.clientapi.clientrequests.extractors.odk.OdkIdentifyExtractor
import com.simprints.clientapi.clientrequests.extractors.odk.OdkVerifyExtractor
import io.mockk.verify

fun OdkActivityTest.odk(func: OdkActivityRobot.() -> Unit) = OdkActivityRobot(rule).apply(func)

class OdkActivityRobot(private val rule: ActivityTestRule<OdkActivity>) {

    infix fun assert(func: OdkActivityAssertions.() -> Unit) {
        OdkActivityAssertions(rule).run(func)
    }

}

class OdkActivityAssertions(private val rule: ActivityTestRule<OdkActivity>) {

    fun toastMessageIsDisplayed() {
        verify(exactly = 1) { rule.activity.guidSelectionNotifier.showMessage() }
    }

    fun enrolExtractorIsOdkEnrolExtractor() {
        assertThat(rule.activity.enrolExtractor).isInstanceOf(OdkEnrolExtractor::class.java)
    }

    fun identifyExtractorIsOdkIdentifyExtractor() {
        assertThat(rule.activity.identifyExtractor).isInstanceOf(OdkIdentifyExtractor::class.java)
    }

    fun verifyExtractorIsOdkVerifyExtractor() {
        assertThat(rule.activity.verifyExtractor).isInstanceOf(OdkVerifyExtractor::class.java)
    }

}
