package com.simprints.clientapi.integration.commcare.responses

import android.app.Activity
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.simprints.clientapi.activities.commcare.CommCareActivity
import com.simprints.clientapi.integration.AppRefusalResponse
import com.simprints.clientapi.integration.BaseClientApiTest
import com.simprints.clientapi.integration.commcare.BaseCommCareClientApiTest
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CommCareExitResponseTest : BaseCommCareClientApiTest() {

    @Test
    fun appModuleSendsAnExitAppResponse_shouldReturnACommCareExitResponse() {
        val appExitResponse = AppRefusalResponse("some_reason", "some_extra")
        mockAppModuleResponse(appExitResponse, APP_ENROL_ACTION)

        val scenario =
            ActivityScenario.launch<CommCareActivity>(commCareBaseIntentRequest.apply { action = COMMCARE_ENROL_ACTION })

        assertCommCareExitResponse(scenario, appExitResponse)
    }

    private fun assertCommCareExitResponse(scenario: ActivityScenario<CommCareActivity>, appExitResponse: AppRefusalResponse) {
        val result = scenario.result
        assertThat(result.resultCode).isEqualTo(Activity.RESULT_OK)
        result.resultData.extras?.getBundle(COMMCARE_BUNDLE_KEY)?.let {
            assertThat(it.getString(EXIT_REASON)).isEqualTo(appExitResponse.reason)
            assertThat(it.getString(EXIT_EXTRA)).isEqualTo(appExitResponse.extra)
            assertThat(it.getString(BIOMETRICS_COMPLETE_KEY)).isEqualTo(BaseClientApiTest.RETURN_FOR_FLOW_COMPLETED.toString())
        } ?: throw Exception("No bundle found")
    }
}
