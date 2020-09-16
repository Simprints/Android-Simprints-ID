package com.simprints.clientapi.integration.commcare.responses

import android.app.Activity
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.simprints.clientapi.activities.commcare.CommCareActivity
import com.simprints.clientapi.integration.*
import com.simprints.clientapi.integration.commcare.BaseCommCareClientApiTest
import com.simprints.moduleapi.app.responses.IAppMatchConfidence
import com.simprints.moduleapi.app.responses.IAppResponseTier
import org.junit.Test
import org.junit.runner.RunWith
import java.util.*

@RunWith(AndroidJUnit4::class)
class CommCareVerifyResponseTest : BaseCommCareClientApiTest() {

    @Test
    fun appModuleSendsAVerifyAppResponse_shouldReturnACommCareVerifyResponse() {
        val appVerifyResponse = AppVerifyResponse(
            AppMatchResult(UUID.randomUUID().toString(), 90, IAppResponseTier.TIER_1, IAppMatchConfidence.HIGH)
        )
        mockAppModuleResponse(appVerifyResponse, APP_VERIFICATION_ACTION)

        val scenario =
            ActivityScenario.launch<CommCareActivity>(commCareBaseFlowIntentRequest.apply {
                action = COMMCARE_VERIFY_ACTION
                putExtra(verifyGuidField.key(), verifyGuidField.value())
            })

        assertCommCareVerifyResponse(scenario, appVerifyResponse)
    }

    private fun assertCommCareVerifyResponse(scenario: ActivityScenario<CommCareActivity>, appVerifyResponse: AppVerifyResponse) {
        val result = scenario.result
        assertThat(result.resultCode).isEqualTo(Activity.RESULT_OK)
        result.resultData.extras?.getBundle(COMMCARE_BUNDLE_KEY)?.let {

            assertThat(it.getString(VERIFICATION_GUID_KEY)).isEqualTo(appVerifyResponse.matchResult.guid)
            assertThat(it.getString(VERIFICATION_TIER_KEY)).isEqualTo(appVerifyResponse.matchResult.tier.name)
            assertThat(it.getString(VERIFICATION_CONFIDENCE_KEY)).isEqualTo(appVerifyResponse.matchResult.confidenceScore.toString())
            assertThat(it.getString(BIOMETRICS_COMPLETE_KEY)).isEqualTo(BaseClientApiTest.RETURN_FOR_FLOW_COMPLETED.toString())
        } ?: throw Exception("No bundle found")
    }
}
