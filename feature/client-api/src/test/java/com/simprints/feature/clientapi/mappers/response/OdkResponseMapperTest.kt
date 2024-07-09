package com.simprints.feature.clientapi.mappers.response

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.simprints.core.domain.response.AppErrorReason
import com.simprints.core.domain.response.AppMatchConfidence
import com.simprints.core.domain.response.AppResponseTier
import com.simprints.feature.clientapi.mappers.request.requestFactories.ConfirmIdentityActionFactory
import com.simprints.feature.clientapi.mappers.request.requestFactories.EnrolActionFactory
import com.simprints.feature.clientapi.mappers.request.requestFactories.EnrolLastBiometricsActionFactory
import com.simprints.feature.clientapi.mappers.request.requestFactories.IdentifyRequestActionFactory
import com.simprints.feature.clientapi.mappers.request.requestFactories.VerifyActionFactory
import com.simprints.feature.clientapi.models.OdkConstants
import com.simprints.infra.orchestration.data.ActionResponse
import com.simprints.infra.orchestration.data.responses.AppMatchResult
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
            identifications = listOf(
                AppMatchResult(
                    guid = "guid-1",
                    confidenceScore = 100,
                    tier = AppResponseTier.TIER_5,
                    matchConfidence = AppMatchConfidence.MEDIUM,
                ),
                AppMatchResult(
                    guid = "guid-2",
                    confidenceScore = 75,
                    tier = AppResponseTier.TIER_3,
                    matchConfidence = AppMatchConfidence.LOW,
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
            identifications = listOf()
        ))

        assertThat(extras.getString(OdkConstants.ODK_HIGHEST_MATCH_CONFIDENCE_FLAG_KEY)).isEqualTo("NONE")
    }

    @Test
    fun `correctly maps confirm response`() {
        val extras = mapper(ActionResponse.ConfirmActionResponse(
            actionIdentifier = ConfirmIdentityActionFactory.getIdentifier(),
            sessionId = "sessionId",
            confirmed = true,
        ))

        assertThat(extras.getString(OdkConstants.ODK_SESSION_ID)).isEqualTo("sessionId")
        assertThat(extras.getBoolean(OdkConstants.ODK_CONFIRM_IDENTITY_BIOMETRICS_COMPLETE)).isEqualTo(true)
    }

    @Test
    fun `correctly maps verify response with null verificationSuccess`() {
        val extras = mapper(ActionResponse.VerifyActionResponse(
            actionIdentifier = VerifyActionFactory.getIdentifier(),
            sessionId = "sessionId",
            matchResult = AppMatchResult(
                guid = "guid",
                confidenceScore = 50,
                tier = AppResponseTier.TIER_2,
                matchConfidence = AppMatchConfidence.HIGH,
                verificationSuccess = null,
            ),
        ))

        assertThat(extras.getString(OdkConstants.ODK_SESSION_ID)).isEqualTo("sessionId")
        assertThat(extras.getString(OdkConstants.ODK_GUIDS_KEY)).isEqualTo("guid")
        assertThat(extras.getString(OdkConstants.ODK_CONFIDENCES_KEY)).isEqualTo("50")
        assertThat(extras.getString(OdkConstants.ODK_TIERS_KEY)).isEqualTo("TIER_2")
        assertThat(extras.getBoolean(OdkConstants.ODK_VERIFY_BIOMETRICS_COMPLETE)).isEqualTo(true)
        assertThat(extras.getBoolean(OdkConstants.ODK_VERIFICATION_SUCCESS_KEY)).isEqualTo(false) // default value
    }

    @Test
    fun `correctly maps verify response with verificationSuccess = false`() {
        val extras = mapper(ActionResponse.VerifyActionResponse(
            actionIdentifier = VerifyActionFactory.getIdentifier(),
            sessionId = "sessionId",
            matchResult = AppMatchResult(
                guid = "guid",
                confidenceScore = 50,
                tier = AppResponseTier.TIER_2,
                matchConfidence = AppMatchConfidence.HIGH,
                verificationSuccess = false,
            ),
        ))

        assertThat(extras.getString(OdkConstants.ODK_SESSION_ID)).isEqualTo("sessionId")
        assertThat(extras.getString(OdkConstants.ODK_GUIDS_KEY)).isEqualTo("guid")
        assertThat(extras.getString(OdkConstants.ODK_CONFIDENCES_KEY)).isEqualTo("50")
        assertThat(extras.getString(OdkConstants.ODK_TIERS_KEY)).isEqualTo("TIER_2")
        assertThat(extras.getBoolean(OdkConstants.ODK_VERIFY_BIOMETRICS_COMPLETE)).isEqualTo(true)
        assertThat(extras.getBoolean(OdkConstants.ODK_VERIFICATION_SUCCESS_KEY)).isEqualTo(false)
    }

    @Test
    fun `correctly maps verify response with verificationSuccess = true`() {
        val extras = mapper(ActionResponse.VerifyActionResponse(
            actionIdentifier = VerifyActionFactory.getIdentifier(),
            sessionId = "sessionId",
            matchResult = AppMatchResult(
                guid = "guid",
                confidenceScore = 50,
                tier = AppResponseTier.TIER_2,
                matchConfidence = AppMatchConfidence.HIGH,
                verificationSuccess = true,
            ),
        ))

        assertThat(extras.getString(OdkConstants.ODK_SESSION_ID)).isEqualTo("sessionId")
        assertThat(extras.getString(OdkConstants.ODK_GUIDS_KEY)).isEqualTo("guid")
        assertThat(extras.getString(OdkConstants.ODK_CONFIDENCES_KEY)).isEqualTo("50")
        assertThat(extras.getString(OdkConstants.ODK_TIERS_KEY)).isEqualTo("TIER_2")
        assertThat(extras.getBoolean(OdkConstants.ODK_VERIFY_BIOMETRICS_COMPLETE)).isEqualTo(true)
        assertThat(extras.getBoolean(OdkConstants.ODK_VERIFICATION_SUCCESS_KEY)).isEqualTo(true)
    }

    @Test
    fun `correctly maps exit form response`() {
        val extras = mapper(ActionResponse.ExitFormActionResponse(
            actionIdentifier = EnrolLastBiometricsActionFactory.getIdentifier(),
            sessionId = "sessionId",
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
            reason = AppErrorReason.UNEXPECTED_ERROR,
            flowCompleted = true,
        ))

        assertThat(extras.getString(OdkConstants.ODK_SESSION_ID)).isEqualTo("sessionId")
        assertThat(extras.getBoolean(OdkConstants.ODK_REGISTER_BIOMETRICS_COMPLETE)).isEqualTo(true)
    }
}
