package com.simprints.clientapi.activities.odk

import android.content.Intent
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import com.simprints.clientapi.activities.robots.odk
import com.simprints.clientapi.di.KoinInjector.loadClientApiKoinModules
import com.simprints.clientapi.di.KoinInjector.unloadClientApiKoinModules
import com.simprints.clientapi.identity.OdkGuidSelectionNotifier
import com.simprints.libsimprints.Constants.*
import com.simprints.testtools.android.BaseActivityTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.test.KoinTest
import org.koin.test.mock.declareMock

@SmallTest
@RunWith(AndroidJUnit4::class)
class OdkActivityTest : BaseActivityTest<OdkActivity>(OdkActivity::class), KoinTest {

    @Before
    override fun setUp() {
        loadClientApiKoinModules()
        declareMock<OdkGuidSelectionNotifier>()
        super.setUp()
    }

    @Test
    fun withConfirmIdentityIntent_shouldDisplayToastMessage() {
        odk {
        } assert {
            toastMessageIsDisplayed()
        }
    }

    override fun tearDown() {
        super.tearDown()
        unloadClientApiKoinModules()
    }

    override fun intent(): Intent {
        return super.intent()
            .setAction(OdkPresenter.ACTION_CONFIRM_IDENTITY)
            .putExtra(SIMPRINTS_PROJECT_ID, "projectId")
            .putExtra(SIMPRINTS_SESSION_ID, "sessionId")
            .putExtra(SIMPRINTS_SELECTED_GUID, "selectedGuid")
    }

}
