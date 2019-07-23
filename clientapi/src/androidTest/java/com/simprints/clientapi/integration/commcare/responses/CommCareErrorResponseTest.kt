package com.simprints.clientapi.integration.commcare.responses

import android.app.Activity
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.simprints.clientapi.activities.commcare.CommCareActivity
import com.simprints.clientapi.integration.AppErrorResponse
import com.simprints.clientapi.integration.BaseClientApiTest
import com.simprints.clientapi.integration.commcare.BaseCommCareClientApiTest
import com.simprints.moduleapi.app.responses.IAppErrorReason
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CommCareErrorResponseTest : BaseCommCareClientApiTest() {

    @Test
    fun appModuleSendsAnErrorAppResponse_shouldReturnACommCareErrorResponse() {
        val appErrorResponse = AppErrorResponse(IAppErrorReason.UNEXPECTED_ERROR)
        mockAppModuleResponse(appErrorResponse, APP_ENROL_ACTION)

        val scenario =
            ActivityScenario.launch<CommCareActivity>(commCareBaseIntentRequest.apply { action = COMMCARE_ENROL_ACTION })

        assertCommCareErrorResponse(scenario)
    }

    private fun assertCommCareErrorResponse(scenario: ActivityScenario<CommCareActivity>) {
        val result = scenario.result
        assertThat(result.resultCode).isEqualTo(Activity.RESULT_OK)
        result.resultData.extras?.getBundle(COMMCARE_BUNDLE_KEY)?.let {
            assertThat(it.getString(SKIP_CHECK_KEY)).isEqualTo(BaseClientApiTest.SKIP_CHECK_VALUE_FOR_COMPLETED_FLOW.toString())
        } ?: throw Exception("No bundle found")
    }
}
