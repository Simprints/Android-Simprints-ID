package com.simprints.clientapi.integration.odk.responses

import android.app.Activity
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.simprints.clientapi.activities.odk.OdkActivity
import com.simprints.clientapi.integration.AppMatchResult
import com.simprints.clientapi.integration.AppVerifyResponse
import com.simprints.clientapi.integration.BaseClientApiTest
import com.simprints.clientapi.integration.odk.BaseOdkClientApiTest
import com.simprints.moduleapi.app.responses.IAppResponseTier
import com.simprints.clientapi.integration.key
import com.simprints.clientapi.integration.value
import org.junit.Test
import org.junit.runner.RunWith
import java.util.*

@RunWith(AndroidJUnit4::class)
class OdkVerifyResponseTest : BaseOdkClientApiTest() {

    @Test
    fun appModuleSendsAVerifyAppResponse_shouldReturnAOdkVerifyResponse() {
        val appVerifyResponse = AppVerifyResponse(
            AppMatchResult(UUID.randomUUID().toString(), 90, IAppResponseTier.TIER_1)
        )
        mockAppModuleResponse(appVerifyResponse, APP_VERIFICATION_ACTION)

        val scenario =
            ActivityScenario.launch<OdkActivity>(odkBaseIntentRequest.apply {
                action = ODK_VERIFY_ACTION
                putExtra(verifyGuidField.key(), verifyGuidField.value())
            })

        assertOdkVerifyResponse(scenario, appVerifyResponse)
    }

    private fun assertOdkVerifyResponse(scenario: ActivityScenario<OdkActivity>, appVerifyResponse: AppVerifyResponse) {
        val result = scenario.result
        assertThat(result.resultCode).isEqualTo(Activity.RESULT_OK)
        result.resultData.extras?.let {
            assertThat(it.getString(ODK_GUIDS_KEY)).isEqualTo(appVerifyResponse.matchResult.guid)
            assertThat(it.getString(ODK_CONFIDENCES_KEY)).isEqualTo(appVerifyResponse.matchResult.confidence.toString())
            assertThat(it.getString(ODK_TIERS_KEY)).isEqualTo(appVerifyResponse.matchResult.tier.name)
            assertThat(it.getBoolean(ODK_BIOMETRICS_COMPLETE_KEY)).isEqualTo(BaseClientApiTest.RETURN_FOR_FLOW_COMPLETED)
        } ?: throw Exception("No bundle found")
    }
}
