package com.simprints.feature.clientapi.mappers.response

import androidx.core.os.bundleOf
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
import com.simprints.feature.clientapi.models.CommCareConstants
import com.simprints.infra.orchestration.data.ActionResponse
import com.simprints.infra.orchestration.data.responses.AppMatchResult
import com.simprints.libsimprints.Constants
import com.simprints.libsimprints.Identification
import com.simprints.libsimprints.Tier
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CommCareResponseMapperTest {

    private val mapper = CommCareResponseMapper()

    @Test
    fun `correctly maps enrol response`() {
        val extras = mapper(ActionResponse.EnrolActionResponse(
            actionIdentifier = EnrolActionFactory.getIdentifier(),
            sessionId = "sessionId",
            enrolledGuid = "guid",
            subjectActions = "subjects"
        )).getBundle(CommCareConstants.COMMCARE_BUNDLE_KEY) ?: bundleOf()

        assertThat(extras.getString(CommCareConstants.SIMPRINTS_SESSION_ID)).isEqualTo("sessionId")
        assertThat(extras.getString(CommCareConstants.REGISTRATION_GUID_KEY)).isEqualTo("guid")
        assertThat(extras.getString(CommCareConstants.BIOMETRICS_COMPLETE_CHECK_KEY)).isEqualTo("true")
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

        assertThat(extras.getString(Constants.SIMPRINTS_SESSION_ID)).isEqualTo("sessionId")
        @Suppress("DEPRECATION")
        //Intentionally using deprecated getParcelableArrayList() as this is what CommCare uses
        assertThat(extras.getParcelableArrayList<Identification>(Constants.SIMPRINTS_IDENTIFICATIONS))
            .isEqualTo(ArrayList<Identification>(
                listOf(
                    Identification("guid-1", 100, Tier.TIER_5),
                    Identification("guid-2", 75, Tier.TIER_3),
                )
            ))
    }

    @Test
    fun `correctly maps confirm response`() {
        val extras = mapper(ActionResponse.ConfirmActionResponse(
            actionIdentifier = ConfirmIdentityActionFactory.getIdentifier(),
            sessionId = "sessionId",
            confirmed = true,
        )).getBundle(CommCareConstants.COMMCARE_BUNDLE_KEY) ?: bundleOf()

        assertThat(extras.getString(CommCareConstants.SIMPRINTS_SESSION_ID)).isEqualTo("sessionId")
        assertThat(extras.getString(CommCareConstants.BIOMETRICS_COMPLETE_CHECK_KEY)).isEqualTo("true")
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
        )).getBundle(CommCareConstants.COMMCARE_BUNDLE_KEY) ?: bundleOf()

        assertThat(extras.getString(CommCareConstants.SIMPRINTS_SESSION_ID)).isEqualTo("sessionId")
        assertThat(extras.getString(CommCareConstants.VERIFICATION_GUID_KEY)).isEqualTo("guid")
        assertThat(extras.getString(CommCareConstants.VERIFICATION_CONFIDENCE_KEY)).isEqualTo("50")
        assertThat(extras.getString(CommCareConstants.VERIFICATION_TIER_KEY)).isEqualTo("TIER_2")
        assertThat(extras.getString(CommCareConstants.BIOMETRICS_COMPLETE_CHECK_KEY)).isEqualTo("true")
        assertThat(extras.getString(CommCareConstants.VERIFICATION_SUCCESS_KEY)).isNull()
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
        )).getBundle(CommCareConstants.COMMCARE_BUNDLE_KEY) ?: bundleOf()

        assertThat(extras.getString(CommCareConstants.SIMPRINTS_SESSION_ID)).isEqualTo("sessionId")
        assertThat(extras.getString(CommCareConstants.VERIFICATION_GUID_KEY)).isEqualTo("guid")
        assertThat(extras.getString(CommCareConstants.VERIFICATION_CONFIDENCE_KEY)).isEqualTo("50")
        assertThat(extras.getString(CommCareConstants.VERIFICATION_TIER_KEY)).isEqualTo("TIER_2")
        assertThat(extras.getString(CommCareConstants.BIOMETRICS_COMPLETE_CHECK_KEY)).isEqualTo("true")
        assertThat(extras.getString(CommCareConstants.VERIFICATION_SUCCESS_KEY)).isEqualTo("false")
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
        )).getBundle(CommCareConstants.COMMCARE_BUNDLE_KEY) ?: bundleOf()

        assertThat(extras.getString(CommCareConstants.SIMPRINTS_SESSION_ID)).isEqualTo("sessionId")
        assertThat(extras.getString(CommCareConstants.VERIFICATION_GUID_KEY)).isEqualTo("guid")
        assertThat(extras.getString(CommCareConstants.VERIFICATION_CONFIDENCE_KEY)).isEqualTo("50")
        assertThat(extras.getString(CommCareConstants.VERIFICATION_TIER_KEY)).isEqualTo("TIER_2")
        assertThat(extras.getString(CommCareConstants.BIOMETRICS_COMPLETE_CHECK_KEY)).isEqualTo("true")
        assertThat(extras.getString(CommCareConstants.VERIFICATION_SUCCESS_KEY)).isEqualTo("true")
    }

    @Test
    fun `correctly maps exit form response`() {
        val extras = mapper(ActionResponse.ExitFormActionResponse(
            actionIdentifier = EnrolLastBiometricsActionFactory.getIdentifier(),
            sessionId = "sessionId",
            reason = "reason",
            extraText = "extra",
        )).getBundle(CommCareConstants.COMMCARE_BUNDLE_KEY) ?: bundleOf()

        assertThat(extras.getString(CommCareConstants.SIMPRINTS_SESSION_ID)).isEqualTo("sessionId")
        assertThat(extras.getString(CommCareConstants.EXIT_REASON)).isEqualTo("reason")
        assertThat(extras.getString(CommCareConstants.EXIT_EXTRA)).isEqualTo("extra")
        assertThat(extras.getString(CommCareConstants.BIOMETRICS_COMPLETE_CHECK_KEY)).isEqualTo("true")
    }

    @Test
    fun `correctly maps error response`() {
        val extras = mapper(ActionResponse.ErrorActionResponse(
            actionIdentifier = EnrolActionFactory.getIdentifier(),
            sessionId = "sessionId",
            reason = AppErrorReason.UNEXPECTED_ERROR,
            flowCompleted = true,
        )).getBundle(CommCareConstants.COMMCARE_BUNDLE_KEY) ?: bundleOf()

        assertThat(extras.getString(CommCareConstants.SIMPRINTS_SESSION_ID)).isEqualTo("sessionId")
        assertThat(extras.getString(CommCareConstants.BIOMETRICS_COMPLETE_CHECK_KEY)).isEqualTo("true")
    }
}
