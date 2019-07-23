package com.simprints.clientapi.integration.standard.responses

import android.app.Activity
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.simprints.clientapi.activities.libsimprints.LibSimprintsActivity
import com.simprints.clientapi.integration.AppRefusalResponse
import com.simprints.clientapi.integration.BaseClientApiTest
import com.simprints.clientapi.integration.standard.BaseStandardClientApiTest
import com.simprints.libsimprints.Constants
import com.simprints.libsimprints.RefusalForm
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class StandardExitResponseTest: BaseStandardClientApiTest() {

    @Test
    fun appModuleSendsAnExitAppResponse_shouldReturnAStandardExitResponse() {
        val appExitResponse = AppRefusalResponse("some_reason", "some_extra")
        mockAppModuleResponse(appExitResponse, APP_ENROL_ACTION)

        val scenario =
            ActivityScenario.launch<LibSimprintsActivity>(standardBaseIntentRequest.apply { action = STANDARD_ENROL_ACTION })

        assertStandardExitResponse(scenario, appExitResponse)
    }

    private fun assertStandardExitResponse(scenario: ActivityScenario<LibSimprintsActivity>, appExitResponse: AppRefusalResponse) {
        val result = scenario.result
        assertThat(result.resultCode).isEqualTo(Activity.RESULT_OK)
        scenario.result.resultData.setExtrasClassLoader(RefusalForm::class.java.classLoader)

        result.resultData.extras?.getParcelable<RefusalForm>(Constants.SIMPRINTS_REFUSAL_FORM)?.let {
            assertThat(it.reason).isEqualTo(appExitResponse.reason)
            assertThat(it.extra).isEqualTo(appExitResponse.extra)
        } ?: throw Exception("No bundle found")
    }
}
