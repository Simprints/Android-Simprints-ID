package com.simprints.clientapi.integration.commcare.responses

import android.app.Activity
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.simprints.clientapi.activities.commcare.CommCareActivity
import com.simprints.clientapi.integration.AppEnrolResponse
import com.simprints.clientapi.integration.BaseClientApiTest
import com.simprints.clientapi.integration.commcare.BaseCommCareClientApiTest
import org.junit.Test
import org.junit.runner.RunWith
import java.util.*

@RunWith(AndroidJUnit4::class)
class CommCareEnrolResponseTest : BaseCommCareClientApiTest() {

    @Test
    fun appModuleSendsAnEnrolAppResponse_shouldReturnACommCareEnrolResponse() {
        val appEnrolResponse = AppEnrolResponse(UUID.randomUUID().toString())
        mockAppModuleResponse(appEnrolResponse, APP_ENROL_ACTION)

        val scenario =
            ActivityScenario.launch<CommCareActivity>(commCareBaseIntentRequest.apply { action = COMMCARE_ENROL_ACTION })

        assertCommCareEnrolResponse(scenario, appEnrolResponse)
    }

    private fun assertCommCareEnrolResponse(scenario: ActivityScenario<CommCareActivity>, appEnrolResponse: AppEnrolResponse) {
        val result = scenario.result
        assertThat(result.resultCode).isEqualTo(Activity.RESULT_OK)
        result.resultData.extras?.getBundle(COMMCARE_BUNDLE_KEY)?.let {
            assertThat(it.getString(REGISTRATION_GUID_KEY)).isEqualTo(appEnrolResponse.guid)
            assertThat(it.getString(BIOMETRICS_COMPLETE_KEY)).isEqualTo(BaseClientApiTest.RETURN_FOR_FLOW_COMPLETED.toString())
        } ?: throw Exception("No bundle found")
    }
}
