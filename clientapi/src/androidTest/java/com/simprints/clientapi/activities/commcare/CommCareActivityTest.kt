package com.simprints.clientapi.activities.commcare

import android.content.Context
import android.content.Intent
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import com.simprints.clientapi.activities.robots.commCare
import com.simprints.clientapi.identity.CommCareGuidSelectionNotifier
import com.simprints.clientapi.integration.BaseClientApiTest
import com.simprints.libsimprints.Constants.*
import io.mockk.mockk
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.test.mock.declare

@RunWith(AndroidJUnit4::class)
class CommCareActivityTest : BaseClientApiTest() {

    @Rule
    @JvmField
    val rule = ActivityTestRule(CommCareActivity::class.java, INITIAL_TOUCH_MODE, LAUNCH_ACTIVITY)

    @Before
    override fun setUp() {
        super.setUp()
        declare {
            factory { (context: Context) -> mockk<CommCareGuidSelectionNotifier>(relaxed = true) }
        }
        rule.launchActivity(buildIntent())
    }

    @Test
    fun withConfirmIdentityIntent_shouldDisplayCorrectToastMessage() {
        commCare {
        } assert {
            toastIsDisplayed()
        }
    }

    private fun buildIntent(): Intent {
        return Intent(CommCarePresenter.ACTION_CONFIRM_IDENTITY)
            .putExtra(SIMPRINTS_PROJECT_ID, "projectId")
            .putExtra(SIMPRINTS_SESSION_ID, "sessionId")
            .putExtra(SIMPRINTS_SELECTED_GUID, "selectedGuid")
    }

    companion object {
        private const val INITIAL_TOUCH_MODE = true
        private const val LAUNCH_ACTIVITY = false
    }

}
