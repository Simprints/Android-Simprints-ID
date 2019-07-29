package com.simprints.clientapi.integration.odk.responses

import android.app.Activity
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.simprints.clientapi.activities.odk.OdkActivity
import com.simprints.clientapi.integration.AppEnrolResponse
import com.simprints.clientapi.integration.odk.BaseOdkClientApiTest
import org.junit.Test
import org.junit.runner.RunWith
import java.util.*

@RunWith(AndroidJUnit4::class)
class OdkEnrolResponseTest : BaseOdkClientApiTest() {

    @Test
    fun appModuleSendsAnEnrolAppResponse_shouldReturnAOdkEnrolResponse() {
        val appEnrolResponse = AppEnrolResponse(UUID.randomUUID().toString())
        mockAppModuleResponse(appEnrolResponse, APP_ENROL_ACTION)

        val scenario =
            ActivityScenario.launch<OdkActivity>(odkBaseIntentRequest.apply { action = ODK_ENROL_ACTION })

        assertOdkEnrolResponse(scenario, appEnrolResponse)
    }

    private fun assertOdkEnrolResponse(scenario: ActivityScenario<OdkActivity>, appEnrolResponse: AppEnrolResponse) {
        val result = scenario.result
        assertThat(result.resultCode).isEqualTo(Activity.RESULT_OK)
        result.resultData.extras?.let {
            assertThat(it.getString(ODK_REGISTRATION_ID_KEY)).isEqualTo(appEnrolResponse.guid)
            assertThat(it.getBoolean(ODK_BIOMETRICS_COMPLETE_KEY)).isEqualTo(RETURN_FOR_FLOW_COMPLETED)
        } ?: throw Exception("No bundle found")
    }
}
