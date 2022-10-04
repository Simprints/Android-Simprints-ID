package com.simprints.clientapi.integration.odk.responses

import android.app.Activity
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.simprints.clientapi.activities.odk.OdkActivity
import com.simprints.clientapi.integration.AppIdentifyResponse
import com.simprints.clientapi.integration.AppMatchResult
import com.simprints.clientapi.integration.odk.BaseOdkClientApiTest
import com.simprints.moduleapi.app.responses.IAppMatchConfidence
import com.simprints.moduleapi.app.responses.IAppMatchResult
import com.simprints.moduleapi.app.responses.IAppResponseTier
import org.junit.Test
import org.junit.runner.RunWith
import java.util.*

@RunWith(AndroidJUnit4::class)
class OdkIdentifyResponseTest : BaseOdkClientApiTest() {

    @Test
    fun appModuleSendsAnIdentifyAppResponse_shouldReturnAOdkIdentifyResponse() {
        val appIdentifyResponse = AppIdentifyResponse(listOf(
            AppMatchResult(UUID.randomUUID().toString(), 90, IAppResponseTier.TIER_1, IAppMatchConfidence.HIGH)
        ), "session_id")
        mockAppModuleResponse(appIdentifyResponse, APP_IDENTIFY_ACTION)

        val scenario =
            ActivityScenario.launch<OdkActivity>(odkBaseFlowIntentRequest.apply { action = ODK_IDENTIFY_ACTION })

        assertOdkIdentifyResponse(scenario, appIdentifyResponse)
    }

    private fun assertOdkIdentifyResponse(scenario: ActivityScenario<OdkActivity>,
                                          appIdentifyResponse: AppIdentifyResponse) {
        val result = scenario.result
        assertThat(result.resultCode).isEqualTo(Activity.RESULT_OK)
        result.resultData.extras?.let {
            assertThat(it.getString(ODK_GUIDS_KEY)).isEqualTo(guidsInOdkFormat(appIdentifyResponse.identifications))
            assertThat(it.getString(ODK_CONFIDENCES_KEY)).isEqualTo(confidenceScoresInOdkFormat(appIdentifyResponse.identifications))
            assertThat(it.getString(ODK_TIERS_KEY)).isEqualTo(tiersInOdkFormat(appIdentifyResponse.identifications))
            assertThat(it.getString(ODK_SESSION_ID)).isEqualTo(appIdentifyResponse.sessionId)
            assertThat(it.getString(ODK_MATCH_CONFIDENCE_FLAGS_KEY)).isEqualTo(confidenceFlagsInOdkFormat(appIdentifyResponse.identifications))
            assertThat(it.getString(ODK_HIGHEST_MATCH_CONFIDENCE_FLAG_KEY)).isEqualTo(getHighestMatchConfidenceAsString(appIdentifyResponse.identifications))
            assertThat(it.getBoolean(ODK_IDENTIFY_BIOMETRICS_COMPLETE)).isEqualTo(RETURN_FOR_FLOW_COMPLETED)
        } ?: throw Exception("No bundle found")
    }

    private fun confidenceScoresInOdkFormat(identifications: List<IAppMatchResult>) =
        identifications.map { it.confidenceScore }.joinToString(separator = " ").trimEnd()

    private fun guidsInOdkFormat(identifications: List<IAppMatchResult>) =
        identifications.map { it.guid }.joinToString(separator = " ").trimEnd()

    private fun tiersInOdkFormat(identifications: List<IAppMatchResult>) =
        identifications.map { it.tier }.joinToString(separator = " ").trimEnd()

    private fun getHighestMatchConfidenceAsString(identifications: List<IAppMatchResult>) =
        when {
            identifications.any { it.matchConfidence == IAppMatchConfidence.HIGH } -> IAppMatchConfidence.HIGH.name
            identifications.any { it.matchConfidence == IAppMatchConfidence.MEDIUM } -> IAppMatchConfidence.MEDIUM.name
            identifications.any { it.matchConfidence == IAppMatchConfidence.LOW } -> IAppMatchConfidence.LOW.name
            else -> IAppMatchConfidence.NONE.name
        }

    private fun confidenceFlagsInOdkFormat(identifications: List<IAppMatchResult>) =
        identifications.map { it.matchConfidence }.joinToString(separator = " ").trimEnd()
}
