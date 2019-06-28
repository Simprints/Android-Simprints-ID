package com.simprints.clientapi.activities.odk

import android.content.Intent
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import com.simprints.clientapi.activities.BaseActivityTest
import com.simprints.clientapi.activities.robots.odk
import com.simprints.id.domain.moduleapi.app.requests.AppIdentityConfirmationRequest
import com.simprints.libsimprints.Constants.*
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock

@SmallTest
@RunWith(AndroidJUnit4::class)
class OdkActivityTest : BaseActivityTest<OdkActivity>(OdkActivity::class) {

    @Mock
    private lateinit var request: AppIdentityConfirmationRequest

    @Test
    fun withConfirmIdentityIntent_shouldDisplayCorrectToastMessage() {
        odk {
        } assert {
            resultSentIsDisplayedOnToast()
        }
    }

    override fun intent(): Intent {
        return super.intent()
            .setAction(OdkPresenter.ACTION_CONFIRM_IDENTITY)
            .putExtra(SIMPRINTS_PROJECT_ID, request.projectId)
            .putExtra(SIMPRINTS_SESSION_ID, request.sessionId)
            .putExtra(SIMPRINTS_SELECTED_GUID, request.selectedGuid)
    }

}
