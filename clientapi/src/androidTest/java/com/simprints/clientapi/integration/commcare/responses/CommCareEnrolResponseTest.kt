package com.simprints.clientapi.integration.commcare.responses

import android.app.Activity
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.simprints.clientapi.activities.commcare.CommCareActivity
import com.simprints.clientapi.integration.AppEnrolResponse
import com.simprints.clientapi.integration.BaseClientApiTest
import com.simprints.clientapi.integration.appEnrolAction
import com.simprints.clientapi.integration.commcare.commCareBaseIntentRequest
import com.simprints.clientapi.integration.commcare.commcareEnrolAction
import org.junit.Test
import org.junit.runner.RunWith
import java.util.*

@RunWith(AndroidJUnit4::class)
class CommCareEnrolResponseTest : BaseClientApiTest() {

    @Test
    fun appModuleSendsAnEnrolAppResponse_shouldReturnACommCareEnrolResponse() {
        val appEnrolResponse = AppEnrolResponse(UUID.randomUUID().toString())
        mockAppModulResponse(appEnrolResponse, appEnrolAction)

        val scenario =
            ActivityScenario.launch<CommCareActivity>(commCareBaseIntentRequest.apply { action = commcareEnrolAction })

        assertCommCareEnrolResponse(scenario, appEnrolResponse)
    }

    private fun assertCommCareEnrolResponse(scenario: ActivityScenario<CommCareActivity>, appEnrolResponse: AppEnrolResponse) {
        val result = scenario.result
        assertThat(result.resultCode).isEqualTo(Activity.RESULT_OK)
        result.resultData.extras?.getBundle("odk_intent_bundle")?.let {
            assertThat(it.getString("guid")).isEqualTo(appEnrolResponse.guid)
            assertThat(it.getBoolean("skipCheck")).isEqualTo(false)
        } ?: throw Exception("No bundle found")
    }
}
