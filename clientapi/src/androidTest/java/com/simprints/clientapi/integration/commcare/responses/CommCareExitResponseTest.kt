package com.simprints.clientapi.integration.commcare.responses

import android.app.Activity
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.simprints.clientapi.activities.commcare.CommCareActivity
import com.simprints.clientapi.integration.APP_ENROL_ACTION
import com.simprints.clientapi.integration.AppRefusalResponse
import com.simprints.clientapi.integration.BaseClientApiTest
import com.simprints.clientapi.integration.SKIP_CHECK_VALUE_FOR_COMPLETED_FLOW
import com.simprints.clientapi.integration.commcare.commCareBaseIntentRequest
import com.simprints.clientapi.integration.commcare.commcareEnrolAction
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CommCareExitResponseTest: BaseClientApiTest() {

    @Test
    fun appModuleSendsAnExitAppResponse_shouldReturnACommCareExitResponse() {
        val appExitResponse = AppRefusalResponse("some_reason", "some_extra")
        mockAppModuleResponse(appExitResponse, APP_ENROL_ACTION)

        val scenario =
            ActivityScenario.launch<CommCareActivity>(commCareBaseIntentRequest.apply { action = commcareEnrolAction })

        assertCommCareExitResponse(scenario, appExitResponse)
    }

    private fun assertCommCareExitResponse(scenario: ActivityScenario<CommCareActivity>, appExitResponse: AppRefusalResponse) {
        val result = scenario.result
        assertThat(result.resultCode).isEqualTo(Activity.RESULT_OK)
        result.resultData.extras?.getBundle("odk_intent_bundle")?.let {
            assertThat(it.getString("reason")).isEqualTo(appExitResponse.reason)
            assertThat(it.getString("extra")).isEqualTo(appExitResponse.extra)
            assertThat(it.getString("skipCheck")).isEqualTo(SKIP_CHECK_VALUE_FOR_COMPLETED_FLOW.toString())
        } ?: throw Exception("No bundle found")
    }
}
