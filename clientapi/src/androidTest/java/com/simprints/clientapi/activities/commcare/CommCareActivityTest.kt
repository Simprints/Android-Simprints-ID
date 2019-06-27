package com.simprints.clientapi.activities.commcare

import android.content.Intent
import androidx.test.filters.SmallTest
import com.simprints.clientapi.activities.BaseActivityTest
import com.simprints.clientapi.activities.robots.commCare
import com.simprints.id.domain.moduleapi.app.requests.AppIdentityConfirmationRequest
import org.junit.Test
import org.mockito.Mock

@SmallTest
class CommCareActivityTest : BaseActivityTest<CommCareActivity>(CommCareActivity::class) {

    @Mock
    private lateinit var request: AppIdentityConfirmationRequest

    @Test
    fun withConfirmIdentityIntent_shouldDisplayCorrectToastMessage() {
        commCare {
        } assert {
            dataSentIsDisplayedOnToast()
        }
    }

    override fun intent(): Intent {
        return super.intent()
            .setAction(CommCarePresenter.ACTION_CONFIRM_IDENTITY)
            .putExtra()
    }

}
