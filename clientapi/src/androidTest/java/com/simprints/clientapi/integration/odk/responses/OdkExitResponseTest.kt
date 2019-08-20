package com.simprints.clientapi.integration.odk.responses

import android.app.Activity
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.simprints.clientapi.activities.odk.OdkActivity
import com.simprints.clientapi.integration.AppRefusalResponse
import com.simprints.clientapi.integration.BaseClientApiTest
import com.simprints.clientapi.integration.odk.BaseOdkClientApiTest
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class OdkExitResponseTest : BaseOdkClientApiTest() {

    @Test
    fun appModuleSendsAnExitAppResponse_shouldReturnAOdkExitResponse() {
        val appExitResponse = AppRefusalResponse("some_reason", "some_extra")
        mockAppModuleResponse(appExitResponse, APP_ENROL_ACTION)

        val scenario =
            ActivityScenario.launch<OdkActivity>(odkBaseIntentRequest.apply { action = ODK_ENROL_ACTION })

        assertOdkExitResponse(scenario, appExitResponse)
    }

    private fun assertOdkExitResponse(scenario: ActivityScenario<OdkActivity>, appExitResponse: AppRefusalResponse) {
        val result = scenario.result
        assertThat(result.resultCode).isEqualTo(Activity.RESULT_OK)
        result.resultData.extras?.let {
            assertThat(it.getString(ODK_EXIT_REASON)).isEqualTo(appExitResponse.reason)
            assertThat(it.getString(ODK_EXIT_EXTRA)).isEqualTo(appExitResponse.extra)
            assertThat(it.getBoolean(ODK_BIOMETRICS_COMPLETE_KEY)).isEqualTo(BaseClientApiTest.RETURN_FOR_FLOW_COMPLETED)
        } ?: throw Exception("No bundle found")
    }
}
