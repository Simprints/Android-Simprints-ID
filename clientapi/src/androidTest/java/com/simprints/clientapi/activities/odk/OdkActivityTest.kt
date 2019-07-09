package com.simprints.clientapi.activities.odk

import android.content.Intent
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import androidx.test.rule.ActivityTestRule
import com.simprints.clientapi.activities.robots.odk
import com.simprints.clientapi.di.KoinInjector.loadClientApiKoinModules
import com.simprints.clientapi.di.KoinInjector.unloadClientApiKoinModules
import com.simprints.clientapi.identity.OdkGuidSelectionNotifier
import com.simprints.libsimprints.Constants.*
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.test.KoinTest
import org.koin.test.mock.declareMock

@SmallTest
@RunWith(AndroidJUnit4::class)
class OdkActivityTest : KoinTest {

    @Rule
    @JvmField
    val rule = ActivityTestRule(OdkActivity::class.java, INITIAL_TOUCH_MODE, LAUNCH_ACTIVITY)

    @Before
    fun setUp() {
        loadClientApiKoinModules()
        declareMock<OdkGuidSelectionNotifier>()
        rule.launchActivity(buildIntent())
    }

    @Test
    fun withConfirmIdentityIntent_shouldDisplayToastMessage() {
        odk {
        } assert {
            toastMessageIsDisplayed()
        }
    }

    @After
    fun tearDown() {
        unloadClientApiKoinModules()
    }

    private fun buildIntent(): Intent {
        return Intent(OdkPresenter.ACTION_CONFIRM_IDENTITY)
            .putExtra(SIMPRINTS_PROJECT_ID, "projectId")
            .putExtra(SIMPRINTS_SESSION_ID, "sessionId")
            .putExtra(SIMPRINTS_SELECTED_GUID, "selectedGuid")
    }

    companion object {
        private const val INITIAL_TOUCH_MODE = true
        private const val LAUNCH_ACTIVITY = false
    }

}
