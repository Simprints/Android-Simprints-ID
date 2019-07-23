package com.simprints.clientapi.integration.standard.responses

import android.app.Activity
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.simprints.clientapi.activities.libsimprints.LibSimprintsActivity
import com.simprints.clientapi.integration.AppEnrolResponse
import com.simprints.clientapi.integration.standard.BaseStandardClientApiTest
import com.simprints.libsimprints.Constants
import com.simprints.libsimprints.Registration
import org.junit.Test
import org.junit.runner.RunWith
import java.util.*

@RunWith(AndroidJUnit4::class)
class StandardEnrolResponseTest : BaseStandardClientApiTest() {

    @Test
    fun appModuleSendsAnEnrolAppResponse_shouldReturnAStandardEnrolResponse() {
        val appEnrolResponse = AppEnrolResponse(UUID.randomUUID().toString())
        mockAppModuleResponse(appEnrolResponse, APP_ENROL_ACTION)

        val scenario =
            ActivityScenario.launch<LibSimprintsActivity>(standardBaseIntentRequest.apply { action = STANDARD_ENROL_ACTION })

        assertStandardEnrolResponse(scenario, appEnrolResponse)
    }

    private fun assertStandardEnrolResponse(scenario: ActivityScenario<LibSimprintsActivity>,
                                            appEnrolResponse: AppEnrolResponse) {
        val result = scenario.result
        assertThat(result.resultCode).isEqualTo(Activity.RESULT_OK)
        scenario.result.resultData.setExtrasClassLoader(Registration::class.java.classLoader)
        result.resultData.extras?.let {
            assertThat(it.getParcelable<Registration>(Constants.SIMPRINTS_REGISTRATION).guid).isEqualTo(appEnrolResponse.guid)
            assertThat(it.getBoolean(Constants.SIMPRINTS_SKIP_CHECK)).isEqualTo(SKIP_CHECK_VALUE_FOR_COMPLETED_FLOW)
        } ?: throw Exception("No bundle found")
    }
}
