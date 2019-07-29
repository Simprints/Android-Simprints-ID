package com.simprints.clientapi.integration.standard.responses

import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.simprints.clientapi.activities.libsimprints.LibSimprintsActivity
import com.simprints.clientapi.integration.AppErrorResponse
import com.simprints.clientapi.integration.standard.BaseStandardClientApiTest
import com.simprints.libsimprints.Constants
import com.simprints.moduleapi.app.responses.IAppErrorReason
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class StandardErrorResponseTest : BaseStandardClientApiTest() {

    @Test
    fun appModuleSendsAnErrorAppResponse_shouldReturnAStandardErrorResponse() {
        val appErrorResponse = AppErrorResponse(IAppErrorReason.UNEXPECTED_ERROR)
        mockAppModuleResponse(appErrorResponse, APP_ENROL_ACTION)

        val scenario =
            ActivityScenario.launch<LibSimprintsActivity>(standardBaseIntentRequest.apply { action = STANDARD_ENROL_ACTION })

        assertStandardErrorResponse(scenario, RETURN_FOR_FLOW_COMPLETED)
    }

    @Test
    fun appModuleSendsAnErrorAppResponseAsLoginNotComplete_shouldReturnAStandardErrorResponse() {
        val appErrorResponse = AppErrorResponse(IAppErrorReason.LOGIN_NOT_COMPLETE)
        mockAppModuleResponse(appErrorResponse, APP_ENROL_ACTION)

        val scenario =
            ActivityScenario.launch<LibSimprintsActivity>(standardBaseIntentRequest.apply { action = STANDARD_ENROL_ACTION })

        assertStandardErrorResponse(scenario, RETURN_FOR_FLOW_NOT_COMPLETED)
    }

    private fun assertStandardErrorResponse(scenario: ActivityScenario<LibSimprintsActivity>,
                                            expectedBiometricsCompleteCheck: Boolean) {
        val result = scenario.result
        assertThat(result.resultCode).isEqualTo(Constants.SIMPRINTS_CANCELLED)
        assertThat(result.resultData.extras?.getBoolean(Constants.SIMPRINTS_BIOMETRICS_COMPLETE_CHECK)).isEqualTo(expectedBiometricsCompleteCheck)
    }
}
