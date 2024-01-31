package com.simprints.clientapi.activities.odk

import android.content.Intent
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import com.simprints.clientapi.activities.odk.OdkAction.OdkActionFollowUpAction.ConfirmIdentity
import com.simprints.clientapi.activities.robots.odk
import com.simprints.clientapi.integration.BaseClientApiTest
import com.simprints.libsimprints.Constants.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class OdkActivityTest : BaseClientApiTest() {

    @Rule
    @JvmField
    val rule = ActivityTestRule(OdkActivity::class.java, INITIAL_TOUCH_MODE, LAUNCH_ACTIVITY)

    @Before
    override fun setUp() {
        super.setUp()
        rule.launchActivity(buildIntent())
    }

    @Test
    fun enrolExtractorShouldBeOdkEnrolExtractor() {
        odk {
        } assert {
            enrolExtractorIsOdkEnrolExtractor()
        }
    }

    @Test
    fun identifyExtractorShouldBeOdkIdentifyExtractor() {
        odk {
        } assert {
            identifyExtractorIsOdkIdentifyExtractor()
        }
    }

    @Test
    fun verifyExtractorShouldBeOdkVerifyExtractor() {
        odk {
        } assert {
            verifyExtractorIsOdkVerifyExtractor()
        }
    }

    private fun buildIntent(): Intent {
        return Intent(ConfirmIdentity.action)
            .putExtra(SIMPRINTS_PROJECT_ID, "xppPLwmR2eUmyN6LS3SN")
            .putExtra(SIMPRINTS_SESSION_ID, "sessionId")
            .putExtra(SIMPRINTS_SELECTED_GUID, "selectedGuid")
    }

    companion object {
        private const val INITIAL_TOUCH_MODE = true
        private const val LAUNCH_ACTIVITY = false
    }

}