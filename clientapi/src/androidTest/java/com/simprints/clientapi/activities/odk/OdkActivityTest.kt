package com.simprints.clientapi.activities.odk

import android.content.Context
import android.content.Intent
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import com.simprints.clientapi.activities.robots.odk
import com.simprints.clientapi.identity.OdkGuidSelectionNotifier
import com.simprints.clientapi.integration.BaseClientApiTest
import com.simprints.libsimprints.Constants.*
import io.mockk.mockk
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.test.mock.declareModule

@RunWith(AndroidJUnit4::class)
class OdkActivityTest : BaseClientApiTest() {

    @Rule
    @JvmField
    val rule = ActivityTestRule(OdkActivity::class.java, INITIAL_TOUCH_MODE, LAUNCH_ACTIVITY)

    @Before
    override fun setUp() {
        super.setUp()
        declareModule {
            factory { (context: Context) -> mockk<OdkGuidSelectionNotifier>(relaxed = true) }
        }
        rule.launchActivity(buildIntent())
    }

    @Test
    fun withConfirmIdentityIntent_shouldDisplayToastMessage() {
        odk {
        } assert {
            toastMessageIsDisplayed()
        }
    }

    private fun buildIntent(): Intent {
        return Intent(OdkAction.ACTION_CONFIRM_IDENTITY)
            .putExtra(SIMPRINTS_PROJECT_ID, "projectId")
            .putExtra(SIMPRINTS_SESSION_ID, "sessionId")
            .putExtra(SIMPRINTS_SELECTED_GUID, "selectedGuid")
    }

    companion object {
        private const val INITIAL_TOUCH_MODE = true
        private const val LAUNCH_ACTIVITY = false
    }

}
