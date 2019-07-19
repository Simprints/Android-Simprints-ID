package com.simprints.clientapi.integration.odk.responses

import android.app.Activity
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.google.common.truth.Truth.assertThat
import com.simprints.clientapi.activities.odk.OdkActivity
import com.simprints.clientapi.integration.APP_ENROL_ACTION
import com.simprints.clientapi.integration.AppErrorResponse
import com.simprints.clientapi.integration.BaseClientApiTest
import com.simprints.clientapi.integration.SKIP_CHECK_VALUE_FOR_COMPLETED_FLOW
import com.simprints.clientapi.integration.odk.odkBaseIntentRequest
import com.simprints.clientapi.integration.odk.odkEnrolAction
import com.simprints.moduleapi.app.responses.IAppErrorReason
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class OdkErrorResponseTest : BaseClientApiTest() {

    @Test
    @MediumTest
    fun appModuleSendsAnErrorAppResponse_shouldReturnAOdkErrorResponse() {
        val appErrorResponse = AppErrorResponse(IAppErrorReason.UNEXPECTED_ERROR)
        mockAppModuleResponse(appErrorResponse, APP_ENROL_ACTION)

        val scenario =
            ActivityScenario.launch<OdkActivity>(odkBaseIntentRequest.apply { action = odkEnrolAction })

        assertOdkErrorResponse(scenario)
    }

    private fun assertOdkErrorResponse(scenario: ActivityScenario<OdkActivity>) {
        val result = scenario.result
        assertThat(result.resultCode).isEqualTo(Activity.RESULT_OK)
        result.resultData.extras?.let {
            assertThat(it.getBoolean("odk-skip-check")).isEqualTo(SKIP_CHECK_VALUE_FOR_COMPLETED_FLOW)
        } ?: throw Exception("No bundle found")
    }
}
