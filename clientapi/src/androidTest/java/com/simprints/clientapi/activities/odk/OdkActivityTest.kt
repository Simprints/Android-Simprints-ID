package com.simprints.clientapi.activities.odk

import android.content.Intent
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import com.simprints.testtools.android.BaseActivityTest
import com.simprints.clientapi.activities.robots.odk
import com.simprints.libsimprints.Constants.*
import org.junit.Test
import org.junit.runner.RunWith

@SmallTest
@RunWith(AndroidJUnit4::class)
class OdkActivityTest : BaseActivityTest<OdkActivity>(OdkActivity::class) {

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
            .putExtra(SIMPRINTS_PROJECT_ID, "projectId")
            .putExtra(SIMPRINTS_SESSION_ID, "sessionId")
            .putExtra(SIMPRINTS_SELECTED_GUID, "selectedGuid")
    }

}
