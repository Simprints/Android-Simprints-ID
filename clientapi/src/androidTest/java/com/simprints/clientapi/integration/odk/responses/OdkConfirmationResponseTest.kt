package com.simprints.clientapi.integration.odk.responses

import android.app.Activity
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth
import com.simprints.clientapi.activities.odk.OdkActivity
import com.simprints.clientapi.integration.AppConfirmationResponse
import com.simprints.clientapi.integration.odk.BaseOdkClientApiTest
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class OdkConfirmationResponseTest : BaseOdkClientApiTest() {

    @Test
    fun appModuleSendsAConfirmationAppResponse_shouldReturnAOdkConfirmationResponse() {
        val appIdentificationOutcomeResponse = AppConfirmationResponse(true)
        mockAppModuleResponse(appIdentificationOutcomeResponse, ODK_CONFIRM_IDENTITY_ACTION)

        val scenario =
            ActivityScenario.launch<OdkActivity>(odkConfirmIntentRequest.apply { action = ODK_CONFIRM_IDENTITY_ACTION })

        assertOdkConfirmationResponse(scenario)
    }

    private fun assertOdkConfirmationResponse(scenario: ActivityScenario<OdkActivity>) {
        val result = scenario.result
        Truth.assertThat(result.resultCode).isEqualTo(Activity.RESULT_OK)
        result.resultData.extras?.let {
            Truth.assertThat(it.getBoolean(ODK_BIOMETRICS_COMPLETE_KEY)).isEqualTo(RETURN_FOR_FLOW_COMPLETED)
        } ?: throw Exception("No bundle found")
    }
}
