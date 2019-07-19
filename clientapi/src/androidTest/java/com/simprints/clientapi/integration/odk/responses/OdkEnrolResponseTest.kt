package com.simprints.clientapi.integration.odk.responses

import android.app.Activity
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.simprints.clientapi.activities.odk.OdkActivity
import com.simprints.clientapi.integration.AppEnrolResponse
import com.simprints.clientapi.integration.BaseClientApiTest
import com.simprints.clientapi.integration.appEnrolAction
import com.simprints.clientapi.integration.odk.odkBaseIntentRequest
import com.simprints.clientapi.integration.odk.odkEnrolAction
import com.simprints.clientapi.integration.skipCheckValueForFlowCompleted
import org.junit.Test
import org.junit.runner.RunWith
import java.util.*

@RunWith(AndroidJUnit4::class)
class OdkEnrolResponseTest : BaseClientApiTest() {

    @Test
    fun appModuleSendsAnEnrolAppResponse_shouldReturnAOdkEnrolResponse() {
        val appEnrolResponse = AppEnrolResponse(UUID.randomUUID().toString())
        mockAppModuleResponse(appEnrolResponse, appEnrolAction)

        val scenario =
            ActivityScenario.launch<OdkActivity>(odkBaseIntentRequest.apply { action = odkEnrolAction })

        assertOdkEnrolResponse(scenario, appEnrolResponse)
    }

    private fun assertOdkEnrolResponse(scenario: ActivityScenario<OdkActivity>, appEnrolResponse: AppEnrolResponse) {
        val result = scenario.result
        assertThat(result.resultCode).isEqualTo(Activity.RESULT_OK)
        result.resultData.extras?.let {
            assertThat(it.getString("odk-registration-id")).isEqualTo(appEnrolResponse.guid)
            assertThat(it.getBoolean("odk-skip-check")).isEqualTo(skipCheckValueForFlowCompleted)

        } ?: throw Exception("No bundle found")
    }
}
