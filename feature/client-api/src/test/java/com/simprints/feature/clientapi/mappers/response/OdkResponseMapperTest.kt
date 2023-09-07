package com.simprints.feature.clientapi.mappers.response

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.simprints.feature.clientapi.mappers.request.requestFactories.ConfirmIdentityActionFactory
import com.simprints.feature.clientapi.mappers.request.requestFactories.EnrolActionFactory
import com.simprints.feature.clientapi.mappers.request.requestFactories.EnrolLastBiometricsActionFactory
import com.simprints.feature.clientapi.mappers.request.requestFactories.IdentifyRequestActionFactory
import com.simprints.feature.clientapi.mappers.request.requestFactories.VerifyActionFactory
import com.simprints.feature.clientapi.models.OdkConstants
import com.simprints.feature.orchestrator.models.ActionResponse
import com.simprints.moduleapi.app.responses.IAppErrorReason
import com.simprints.moduleapi.app.responses.IAppMatchConfidence
import com.simprints.moduleapi.app.responses.IAppResponseTier
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class OdkResponseMapperTest {

    private val mapper = OdkResponseMapper()

    @Test
    fun `correctly maps enrol response`() {
        val extras = mapper(ActionResponse.EnrolActionResponse(
            actionIdentifier = EnrolActionFactory.getIdentifier(),
            sessionId = "sessionId",
            eventsJson = null,
            enrolledGuid = "guid",
            subjectActions = "subjects"
        ))

        assertThat(extras.getString(OdkConstants.ODK_SESSION_ID)).isEqualTo("sessionId")
        assertThat(extras.getString(OdkConstants.ODK_REGISTRATION_ID_KEY)).isEqualTo("guid")
        assertThat(extras.getBoolean(OdkConstants.ODK_REGISTER_BIOMETRICS_COMPLETE)).isEqualTo(true)
    }

    @Test
    fun `correctly maps identify response`() {
        val extras = mapper(ActionResponse.IdentifyActionResponse(
            actionIdentifier = IdentifyRequestActionFactory.getIdentifier(),
            sessionId = "sessionId",
            eventsJson = null,
            identifications = listOf(
                StubMatchResult(
                    guid = "guid-1",
                    confidenceScore = 100,
                    tier = IAppResponseTier.TIER_5,
                    matchConfidence = IAppMatchConfidence.MEDIUM,
                ),
                StubMatchResult(
                    guid = "guid-2",
                    confidenceScore = 75,
                    tier = IAppResponseTier.TIER_3,
                    matchConfidence = IAppMatchConfidence.LOW,
                ),
            )
        ))

        assertThat(extras.getString(OdkConstants.ODK_SESSION_ID)).isEqualTo("sessionId")
        assertThat(extras.getString(OdkConstants.ODK_GUIDS_KEY)).isEqualTo("guid-1 guid-2")
        assertThat(extras.getString(OdkConstants.ODK_CONFIDENCES_KEY)).isEqualTo("100 75")
        assertThat(extras.getString(OdkConstants.ODK_TIERS_KEY)).isEqualTo("TIER_5 TIER_3")
        assertThat(extras.getString(OdkConstants.ODK_MATCH_CONFIDENCE_FLAGS_KEY)).isEqualTo("MEDIUM LOW")
        assertThat(extras.getString(OdkConstants.ODK_HIGHEST_MATCH_CONFIDENCE_FLAG_KEY)).isEqualTo("MEDIUM")
        assertThat(extras.getBoolean(OdkConstants.ODK_IDENTIFY_BIOMETRICS_COMPLETE)).isEqualTo(true)
    }

    @Test
    fun `correctly finds correct highest confidence for empty list`() {
        val extras = mapper(ActionResponse.IdentifyActionResponse(
            actionIdentifier = IdentifyRequestActionFactory.getIdentifier(),
            sessionId = "sessionId",
            eventsJson = null,
            identifications = listOf()
        ))

        assertThat(extras.getString(OdkConstants.ODK_HIGHEST_MATCH_CONFIDENCE_FLAG_KEY)).isEqualTo("NONE")
    }

    @Test
    fun `correctly maps confirm response`() {
        val extras = mapper(ActionResponse.ConfirmActionResponse(
            actionIdentifier = ConfirmIdentityActionFactory.getIdentifier(),
            sessionId = "sessionId",
            eventsJson = null,
            confirmed = true,
        ))

        assertThat(extras.getString(OdkConstants.ODK_SESSION_ID)).isEqualTo("sessionId")
        assertThat(extras.getBoolean(OdkConstants.ODK_CONFIRM_IDENTITY_BIOMETRICS_COMPLETE)).isEqualTo(true)
    }

    @Test
    fun `correctly maps verify response`() {
        val extras = mapper(ActionResponse.VerifyActionResponse(
            actionIdentifier = VerifyActionFactory.getIdentifier(),
            sessionId = "sessionId",
            eventsJson = null,
            matchResult = StubMatchResult(
                guid = "guid",
                confidenceScore = 50,
                tier = IAppResponseTier.TIER_2,
                matchConfidence = IAppMatchConfidence.HIGH,
            ),
        ))

        assertThat(extras.getString(OdkConstants.ODK_SESSION_ID)).isEqualTo("sessionId")
        assertThat(extras.getString(OdkConstants.ODK_GUIDS_KEY)).isEqualTo("guid")
        assertThat(extras.getString(OdkConstants.ODK_CONFIDENCES_KEY)).isEqualTo("50")
        assertThat(extras.getString(OdkConstants.ODK_TIERS_KEY)).isEqualTo("TIER_2")
        assertThat(extras.getBoolean(OdkConstants.ODK_VERIFY_BIOMETRICS_COMPLETE)).isEqualTo(true)
    }

    @Test
    fun `correctly maps exit form response`() {
        val extras = mapper(ActionResponse.ExitFormActionResponse(
            actionIdentifier = EnrolLastBiometricsActionFactory.getIdentifier(),
            sessionId = "sessionId",
            eventsJson = null,
            reason = "reason",
            extraText = "extra",
        ))

        assertThat(extras.getString(OdkConstants.ODK_SESSION_ID)).isEqualTo("sessionId")
        assertThat(extras.getString(OdkConstants.ODK_EXIT_REASON)).isEqualTo("reason")
        assertThat(extras.getString(OdkConstants.ODK_EXIT_EXTRA)).isEqualTo("extra")
        assertThat(extras.getBoolean(OdkConstants.ODK_REGISTER_BIOMETRICS_COMPLETE)).isEqualTo(true)
    }

    @Test
    fun `correctly maps error response`() {
        val extras = mapper(ActionResponse.ErrorActionResponse(
            actionIdentifier = EnrolActionFactory.getIdentifier(),
            sessionId = "sessionId",
            eventsJson = null,
            reason = IAppErrorReason.UNEXPECTED_ERROR,
            flowCompleted = true,
        ))

        assertThat(extras.getString(OdkConstants.ODK_SESSION_ID)).isEqualTo("sessionId")
        assertThat(extras.getBoolean(OdkConstants.ODK_REGISTER_BIOMETRICS_COMPLETE)).isEqualTo(true)
    }
}
