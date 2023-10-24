package com.simprints.feature.clientapi.mappers.response

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.simprints.feature.clientapi.mappers.request.requestFactories.ConfirmIdentityActionFactory
import com.simprints.feature.clientapi.mappers.request.requestFactories.EnrolActionFactory
import com.simprints.feature.clientapi.mappers.request.requestFactories.EnrolLastBiometricsActionFactory
import com.simprints.feature.clientapi.mappers.request.requestFactories.IdentifyRequestActionFactory
import com.simprints.feature.clientapi.mappers.request.requestFactories.VerifyActionFactory
import com.simprints.infra.orchestration.data.ActionResponse
import com.simprints.libsimprints.Constants
import com.simprints.libsimprints.Identification
import com.simprints.libsimprints.RefusalForm
import com.simprints.libsimprints.Registration
import com.simprints.libsimprints.Tier
import com.simprints.libsimprints.Verification
import com.simprints.moduleapi.app.responses.IAppErrorReason
import com.simprints.moduleapi.app.responses.IAppMatchConfidence
import com.simprints.moduleapi.app.responses.IAppResponseTier
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LibSimprintsResponseMapperTest {

    private val mapper = LibSimprintsResponseMapper()

    @Test
    fun `correctly maps enrol response`() {
        val extras = mapper(ActionResponse.EnrolActionResponse(
            actionIdentifier = EnrolActionFactory.getIdentifier(),
            sessionId = "sessionId",
            eventsJson = null,
            enrolledGuid = "guid",
            subjectActions = "subjects"
        ))

        assertThat(extras.getString(Constants.SIMPRINTS_SESSION_ID)).isEqualTo("sessionId")
        assertThat(extras.getParcelable<Registration>(Constants.SIMPRINTS_REGISTRATION)).isEqualTo(Registration("guid"))
        assertThat(extras.getBoolean(Constants.SIMPRINTS_BIOMETRICS_COMPLETE_CHECK)).isEqualTo(true)
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

        assertThat(extras.getString(Constants.SIMPRINTS_SESSION_ID)).isEqualTo("sessionId")
        assertThat(extras.getParcelableArray(Constants.SIMPRINTS_IDENTIFICATIONS)).isEqualTo(arrayOf(
            Identification("guid-1", 100, Tier.TIER_5),
            Identification("guid-2", 75, Tier.TIER_3),
        ))
    }

    @Test
    fun `correctly maps confirm response`() {
        val extras = mapper(ActionResponse.ConfirmActionResponse(
            actionIdentifier = ConfirmIdentityActionFactory.getIdentifier(),
            sessionId = "sessionId",
            eventsJson = null,
            confirmed = true,
        ))

        assertThat(extras.getString(Constants.SIMPRINTS_SESSION_ID)).isEqualTo("sessionId")
        assertThat(extras.getBoolean(Constants.SIMPRINTS_BIOMETRICS_COMPLETE_CHECK)).isEqualTo(true)
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

        // Verification does not implement equals, so we have to check each field individually
        val extraVerification = extras.getParcelable<Verification>(Constants.SIMPRINTS_VERIFICATION)

        assertThat(extras.getString(Constants.SIMPRINTS_SESSION_ID)).isEqualTo("sessionId")
        assertThat(extraVerification?.guid).isEqualTo("guid")
        assertThat(extraVerification?.tier).isEqualTo(Tier.TIER_2)
        assertThat(extraVerification?.confidence).isEqualTo(50)
        assertThat(extras.getBoolean(Constants.SIMPRINTS_BIOMETRICS_COMPLETE_CHECK)).isEqualTo(true)
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

        assertThat(extras.getString(Constants.SIMPRINTS_SESSION_ID)).isEqualTo("sessionId")
        assertThat(extras.getParcelable<RefusalForm>(Constants.SIMPRINTS_REFUSAL_FORM)).isEqualTo(RefusalForm("reason", "extra"))
        assertThat(extras.getBoolean(Constants.SIMPRINTS_BIOMETRICS_COMPLETE_CHECK)).isEqualTo(true)
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

        assertThat(extras.getString(Constants.SIMPRINTS_SESSION_ID)).isEqualTo("sessionId")
        assertThat(extras.getBoolean(Constants.SIMPRINTS_BIOMETRICS_COMPLETE_CHECK)).isEqualTo(true)
        assertThat(extras.getInt(LibSimprintsResponseMapper.RESULT_CODE_OVERRIDE)).isEqualTo(Constants.SIMPRINTS_UNEXPECTED_ERROR)
    }

    @Test
    fun `correctly maps error code override`() {
        mapOf(
            IAppErrorReason.UNEXPECTED_ERROR to Constants.SIMPRINTS_UNEXPECTED_ERROR,
            IAppErrorReason.ROOTED_DEVICE to Constants.SIMPRINTS_ROOTED_DEVICE,
            IAppErrorReason.LOGIN_NOT_COMPLETE to Constants.SIMPRINTS_LOGIN_NOT_COMPLETE,
            IAppErrorReason.DIFFERENT_PROJECT_ID_SIGNED_IN to Constants.SIMPRINTS_INVALID_PROJECT_ID,
            IAppErrorReason.DIFFERENT_USER_ID_SIGNED_IN to Constants.SIMPRINTS_INVALID_USER_ID,
            IAppErrorReason.GUID_NOT_FOUND_ONLINE to Constants.SIMPRINTS_VERIFY_GUID_NOT_FOUND_ONLINE,
            IAppErrorReason.GUID_NOT_FOUND_OFFLINE to Constants.SIMPRINTS_VERIFY_GUID_NOT_FOUND_OFFLINE,
            IAppErrorReason.ENROLMENT_LAST_BIOMETRICS_FAILED to Constants.SIMPRINTS_ENROLMENT_LAST_BIOMETRICS_FAILED,
            IAppErrorReason.BLUETOOTH_NOT_SUPPORTED to Constants.SIMPRINTS_BLUETOOTH_NOT_SUPPORTED,
            IAppErrorReason.BLUETOOTH_NO_PERMISSION to Constants.SIMPRINTS_BLUETOOTH_NO_PERMISSION,
            IAppErrorReason.FINGERPRINT_CONFIGURATION_ERROR to Constants.SIMPRINTS_FINGERPRINT_CONFIGURATION_ERROR,
            IAppErrorReason.FACE_CONFIGURATION_ERROR to Constants.SIMPRINTS_FACE_CONFIGURATION_ERROR,
            IAppErrorReason.FACE_LICENSE_MISSING to Constants.SIMPRINTS_FACE_LICENSE_MISSING,
            IAppErrorReason.FACE_LICENSE_INVALID to Constants.SIMPRINTS_FACE_LICENSE_INVALID,
            IAppErrorReason.BACKEND_MAINTENANCE_ERROR to Constants.SIMPRINTS_BACKEND_MAINTENANCE_ERROR,
            IAppErrorReason.PROJECT_PAUSED to Constants.SIMPRINTS_PROJECT_PAUSED,
            IAppErrorReason.PROJECT_ENDING to Constants.SIMPRINTS_PROJECT_ENDING,
        ).forEach { (reason, expectedCode) ->
            val extras = mapper(ActionResponse.ErrorActionResponse(
                actionIdentifier = EnrolActionFactory.getIdentifier(),
                sessionId = "sessionId",
                eventsJson = null,
                reason = reason,
                flowCompleted = true,
            ))

            assertThat(extras.getInt(LibSimprintsResponseMapper.RESULT_CODE_OVERRIDE)).isEqualTo(expectedCode)
        }
    }
}
