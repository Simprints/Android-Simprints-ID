package com.simprints.clientapi.integration.odk.responses

import android.app.Activity
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.simprints.clientapi.activities.odk.OdkActivity
import com.simprints.clientapi.integration.*
import com.simprints.clientapi.integration.odk.odkBaseIntentRequest
import com.simprints.clientapi.integration.odk.odkIdentifyAction
import com.simprints.moduleapi.app.responses.IAppMatchResult
import com.simprints.moduleapi.app.responses.IAppResponseTier
import org.junit.Test
import org.junit.runner.RunWith
import java.util.*

@RunWith(AndroidJUnit4::class)
class OdkIdentifyResponseTest : BaseClientApiTest() {

    @Test
    fun appModuleSendsAnIdentifyAppResponse_shouldReturnAOdkIdentifyResponse() {
        val appIdentifyResponse = AppIdentifyResponse(listOf(
            AppMatchResult(UUID.randomUUID().toString(), 90, IAppResponseTier.TIER_1)
        ), "session_id")
        mockAppModuleResponse(appIdentifyResponse, appIdentifyAction)

        val scenario =
            ActivityScenario.launch<OdkActivity>(odkBaseIntentRequest.apply { action = odkIdentifyAction })

        assertOdkIdentifyResponse(scenario, appIdentifyResponse)
    }

    private fun assertOdkIdentifyResponse(scenario: ActivityScenario<OdkActivity>,
                                          appIdentifyResponse: AppIdentifyResponse) {
        val result = scenario.result
        assertThat(result.resultCode).isEqualTo(Activity.RESULT_OK)
        result.resultData.extras?.let {
            assertThat(it.getString("odk-guids")).isEqualTo(guidsInOdkFormat(appIdentifyResponse.identifications))
            assertThat(it.getString("odk-confidences")).isEqualTo(confidenceScoresInOdkFormat(appIdentifyResponse.identifications))
            assertThat(it.getString("odk-tiers")).isEqualTo(tiersInOdkFormat(appIdentifyResponse.identifications))
            assertThat(it.getString("odk-session-id")).isEqualTo(appIdentifyResponse.sessionId)
            assertThat(it.getBoolean("odk-skip-check")).isEqualTo(skipCheckValueForFlowCompleted)
        } ?: throw Exception("No bundle found")
    }

    private fun confidenceScoresInOdkFormat(identifications: List<IAppMatchResult>) =
        identifications.joinToString(separator = "", transform = { "${it.confidence} " }).trimEnd()

    private fun guidsInOdkFormat(identifications: List<IAppMatchResult>) =
        identifications.joinToString(separator = "", transform = { "${it.guid} " }).trimEnd()

    private fun tiersInOdkFormat(identifications: List<IAppMatchResult>) =
        identifications.joinToString(separator = "", transform = { "${it.tier} " }).trimEnd()
}
