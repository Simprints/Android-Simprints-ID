package com.simprints.clientapi.integration.odk.responses

import android.app.Activity
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.simprints.clientapi.activities.odk.OdkActivity
import com.simprints.clientapi.integration.AppErrorResponse
import com.simprints.clientapi.integration.odk.BaseOdkClientApiTest
import com.simprints.moduleapi.app.responses.IAppErrorReason
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class OdkErrorResponseTest : BaseOdkClientApiTest() {

    @Test
    fun appModuleSendsAnErrorAppResponse_shouldReturnAOdkErrorResponse() {
        val appErrorResponse = AppErrorResponse(IAppErrorReason.UNEXPECTED_ERROR)
        mockAppModuleResponse(appErrorResponse, APP_ENROL_ACTION)

        val scenario =
            ActivityScenario.launch<OdkActivity>(odkBaseIntentRequest.apply { action = ODK_ENROL_ACTION })

        assertOdkErrorResponse(scenario, SKIP_CHECK_VALUE_FOR_COMPLETED_FLOW)
    }

    @Test
    fun appModuleSendsAnErrorAppResponseLoginNotComplete_shouldReturnAOdkErrorResponse() {
        val appErrorResponse = AppErrorResponse(IAppErrorReason.LOGIN_NOT_COMPLETE)
        mockAppModuleResponse(appErrorResponse, APP_ENROL_ACTION)

        val scenario =
            ActivityScenario.launch<OdkActivity>(odkBaseIntentRequest.apply { action = ODK_ENROL_ACTION })

        assertOdkErrorResponse(scenario, SKIP_CHECK_VALUE_FOR_NOT_COMPLETED_FLOW)
    }
    private fun assertOdkErrorResponse(scenario: ActivityScenario<OdkActivity>, expectedSkipCheck: Boolean) {
        val result = scenario.result
        assertThat(result.resultCode).isEqualTo(Activity.RESULT_OK)
        result.resultData.extras?.let {
            assertThat(it.getBoolean(ODK_SKIP_CHECK_KEY)).isEqualTo(expectedSkipCheck)
        } ?: throw Exception("No bundle found")
    }
}
