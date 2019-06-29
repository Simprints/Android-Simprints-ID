package com.simprints.clientapi.integration.odk.responses

import android.app.Activity
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.simprints.clientapi.activities.odk.OdkActivity
import com.simprints.clientapi.integration.AppRefusalResponse
import com.simprints.clientapi.integration.BaseClientApiTest
import com.simprints.clientapi.integration.appEnrolAction
import com.simprints.clientapi.integration.odk.odkBaseIntentRequest
import com.simprints.clientapi.integration.odk.odkEnrolAction
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class OdkExitResponseTest : BaseClientApiTest() {

    @Test
    fun appModuleSendsAnExitAppResponse_shouldReturnAOdkExitResponse() {
        val appExitResponse = AppRefusalResponse("some_reason", "some_extra")
        mockAppModulResponse(appExitResponse, appEnrolAction)

        val scenario =
            ActivityScenario.launch<OdkActivity>(odkBaseIntentRequest.apply { action = odkEnrolAction })

        assertOdkExitResponse(scenario, appExitResponse)
    }

    private fun assertOdkExitResponse(scenario: ActivityScenario<OdkActivity>, appExitResponse: AppRefusalResponse) {
        val result = scenario.result
        assertThat(result.resultCode).isEqualTo(Activity.RESULT_OK)
        result.resultData.extras?.let {
            assertThat(it.getString("odk-exit-reason")).isEqualTo(appExitResponse.reason)
            assertThat(it.getString("odk-exit-extra")).isEqualTo(appExitResponse.extra)
            assertThat(it.getBoolean("odk-skip-check")).isEqualTo(false)
        } ?: throw Exception("No bundle found")
    }
}
