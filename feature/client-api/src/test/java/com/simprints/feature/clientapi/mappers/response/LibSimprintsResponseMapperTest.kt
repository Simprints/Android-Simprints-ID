package com.simprints.feature.clientapi.mappers.response

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.simprints.core.domain.response.AppErrorReason
import com.simprints.core.domain.response.AppMatchConfidence
import com.simprints.feature.clientapi.mappers.request.requestFactories.ConfirmIdentityActionFactory
import com.simprints.feature.clientapi.mappers.request.requestFactories.EnrolActionFactory
import com.simprints.feature.clientapi.mappers.request.requestFactories.EnrolLastBiometricsActionFactory
import com.simprints.feature.clientapi.mappers.request.requestFactories.IdentifyRequestActionFactory
import com.simprints.feature.clientapi.mappers.request.requestFactories.VerifyActionFactory
import com.simprints.infra.orchestration.data.ActionResponse
import com.simprints.infra.orchestration.data.responses.AppMatchResult
import com.simprints.libsimprints.Constants
import com.simprints.libsimprints.contracts.VersionsList
import org.junit.Test
import org.junit.runner.RunWith
import com.simprints.libsimprints.Identification as LegacyIdentification
import com.simprints.libsimprints.RefusalForm as LegacyRefusalForm
import com.simprints.libsimprints.Registration as LegacyEnrolment
import com.simprints.libsimprints.Tier as LegacyTier
import com.simprints.libsimprints.Verification as LegacyVerification

@RunWith(AndroidJUnit4::class)
class LibSimprintsResponseMapperTest {
    private val mapper = LibSimprintsResponseMapper()

    @Test
    fun `correctly maps enrol response`() {
        val extras = mapper(
            ActionResponse.EnrolActionResponse(
                actionIdentifier = EnrolActionFactory.getIdentifier(),
                sessionId = "sessionId",
                enrolledGuid = "guid",
                subjectActions = "subjects",
            ),
        )

        assertThat(extras.getString(Constants.SIMPRINTS_SESSION_ID)).isEqualTo("sessionId")
        assertThat(extras.getParcelable<LegacyEnrolment>(Constants.SIMPRINTS_REGISTRATION)).isEqualTo(
            LegacyEnrolment("guid"),
        )
        assertThat(extras.getBoolean(Constants.SIMPRINTS_BIOMETRICS_COMPLETE_CHECK)).isTrue()
    }

    @Test
    fun `correctly maps enrol response after LibSimprints refactoring`() {
        val extras = mapper(
            ActionResponse.EnrolActionResponse(
                actionIdentifier = EnrolActionFactory
                    .getIdentifier()
                    .copy(contractVersion = VersionsList.INITIAL_REWORK),
                sessionId = "sessionId",
                enrolledGuid = "guid",
                subjectActions = "subjects",
            ),
        )

        assertThat(extras.getString(Constants.SIMPRINTS_SESSION_ID)).isEqualTo("sessionId")
        assertThat(extras.getString(Constants.SIMPRINTS_ENROLMENT)).isEqualTo("""{"guid":"guid"}""")
        assertThat(extras.getBoolean(Constants.SIMPRINTS_BIOMETRICS_COMPLETE_CHECK)).isTrue()
    }

    @Test
    fun `correctly maps identify response`() {
        val extras = mapper(
            ActionResponse.IdentifyActionResponse(
                actionIdentifier = IdentifyRequestActionFactory.getIdentifier(),
                sessionId = "sessionId",
                identifications = listOf(
                    AppMatchResult(
                        guid = "guid-1",
                        confidenceScore = 100,
                        matchConfidence = AppMatchConfidence.MEDIUM,
                    ),
                    AppMatchResult(
                        guid = "guid-2",
                        confidenceScore = 75,
                        matchConfidence = AppMatchConfidence.LOW,
                    ),
                ),
            ),
        )

        assertThat(extras.getString(Constants.SIMPRINTS_SESSION_ID)).isEqualTo("sessionId")
        assertThat(extras.getParcelableArrayList<LegacyIdentification>(Constants.SIMPRINTS_IDENTIFICATIONS)).containsExactly(
            LegacyIdentification("guid-1", 100, LegacyTier.TIER_2),
            LegacyIdentification("guid-2", 75, LegacyTier.TIER_3),
        )
    }

    @Test
    fun `correctly maps identify response after LibSimprints refactoring`() {
        val extras = mapper(
            ActionResponse.IdentifyActionResponse(
                actionIdentifier = IdentifyRequestActionFactory
                    .getIdentifier()
                    .copy(contractVersion = VersionsList.INITIAL_REWORK),
                sessionId = "sessionId",
                identifications = listOf(
                    AppMatchResult(
                        guid = "guid-1",
                        confidenceScore = 100,
                        matchConfidence = AppMatchConfidence.MEDIUM,
                    ),
                ),
            ),
        )

        assertThat(extras.getString(Constants.SIMPRINTS_SESSION_ID)).isEqualTo("sessionId")
        assertThat(extras.getString(Constants.SIMPRINTS_IDENTIFICATIONS)).isEqualTo(
            """
            [{"guid":"guid-1","confidenceBand":"MEDIUM","confidence":100}]
            """.trimIndent(),
        )
        assertThat(extras.getBoolean(Constants.SIMPRINTS_BIOMETRICS_COMPLETE_CHECK)).isTrue()
    }

    @Test
    fun `correctly maps confirm response`() {
        val extras = mapper(
            ActionResponse.ConfirmActionResponse(
                actionIdentifier = ConfirmIdentityActionFactory.getIdentifier(),
                sessionId = "sessionId",
                confirmed = true,
            ),
        )

        assertThat(extras.getString(Constants.SIMPRINTS_SESSION_ID)).isEqualTo("sessionId")
        assertThat(extras.getBoolean(Constants.SIMPRINTS_BIOMETRICS_COMPLETE_CHECK)).isTrue()
    }

    @Test
    fun `correctly maps verify response with null verificationSuccess`() {
        val extras = mapper(
            ActionResponse.VerifyActionResponse(
                actionIdentifier = VerifyActionFactory.getIdentifier(),
                sessionId = "sessionId",
                matchResult = AppMatchResult(
                    guid = "guid",
                    confidenceScore = 50,
                    matchConfidence = AppMatchConfidence.HIGH,
                    verificationSuccess = null,
                ),
            ),
        )

        // Verification does not implement equals, so we have to check each field individually
        val extraVerification = extras.getParcelable<LegacyVerification>(Constants.SIMPRINTS_VERIFICATION)

        assertThat(extras.getString(Constants.SIMPRINTS_SESSION_ID)).isEqualTo("sessionId")
        assertThat(extraVerification?.guid).isEqualTo("guid")
        assertThat(extraVerification?.tier).isEqualTo(LegacyTier.TIER_1)
        assertThat(extraVerification?.getConfidence()).isEqualTo(50)
        assertThat(extras.getBoolean(Constants.SIMPRINTS_BIOMETRICS_COMPLETE_CHECK)).isTrue()
        assertThat(extras.keySet()).doesNotContain(Constants.SIMPRINTS_VERIFICATION_SUCCESS)
    }

    @Test
    fun `correctly maps verify response with verificationSuccess = false`() {
        val extras = mapper(
            ActionResponse.VerifyActionResponse(
                actionIdentifier = VerifyActionFactory.getIdentifier(),
                sessionId = "sessionId",
                matchResult = AppMatchResult(
                    guid = "guid",
                    confidenceScore = 50,
                    matchConfidence = AppMatchConfidence.HIGH,
                    verificationSuccess = false,
                ),
            ),
        )

        // Verification does not implement equals, so we have to check each field individually
        val extraVerification = extras.getParcelable<LegacyVerification>(Constants.SIMPRINTS_VERIFICATION)

        assertThat(extras.getString(Constants.SIMPRINTS_SESSION_ID)).isEqualTo("sessionId")
        assertThat(extraVerification?.guid).isEqualTo("guid")
        assertThat(extraVerification?.tier).isEqualTo(LegacyTier.TIER_1)
        assertThat(extraVerification?.getConfidence()).isEqualTo(50)
        assertThat(extras.getBoolean(Constants.SIMPRINTS_BIOMETRICS_COMPLETE_CHECK)).isTrue()
        assertThat(extras.keySet()).contains(Constants.SIMPRINTS_VERIFICATION_SUCCESS)
        assertThat(extras.getBoolean(Constants.SIMPRINTS_VERIFICATION_SUCCESS)).isEqualTo(false)
    }

    @Test
    fun `correctly maps verify response with verificationSuccess = true`() {
        val extras = mapper(
            ActionResponse.VerifyActionResponse(
                actionIdentifier = VerifyActionFactory.getIdentifier().copy(contractVersion = 2),
                sessionId = "sessionId",
                matchResult = AppMatchResult(
                    guid = "guid",
                    confidenceScore = 50,
                    matchConfidence = AppMatchConfidence.HIGH,
                    verificationSuccess = true,
                ),
            ),
        )

        // Verification does not implement equals, so we have to check each field individually
        val extraVerification = extras.getParcelable<LegacyVerification>(Constants.SIMPRINTS_VERIFICATION)

        assertThat(extras.getString(Constants.SIMPRINTS_SESSION_ID)).isEqualTo("sessionId")
        assertThat(extraVerification?.guid).isEqualTo("guid")
        assertThat(extraVerification?.tier).isEqualTo(LegacyTier.TIER_1)
        assertThat(extraVerification?.getConfidence()).isEqualTo(50)
        assertThat(extras.getBoolean(Constants.SIMPRINTS_BIOMETRICS_COMPLETE_CHECK)).isTrue()
        assertThat(extras.getBoolean(Constants.SIMPRINTS_VERIFICATION_SUCCESS)).isTrue()
    }

    @Test
    fun `correctly maps verify response after LibSimprints refactoring`() {
        val extras = mapper(
            ActionResponse.VerifyActionResponse(
                actionIdentifier = VerifyActionFactory
                    .getIdentifier()
                    .copy(contractVersion = VersionsList.INITIAL_REWORK),
                sessionId = "sessionId",
                matchResult = AppMatchResult(
                    guid = "guid",
                    confidenceScore = 50,
                    matchConfidence = AppMatchConfidence.HIGH,
                    verificationSuccess = true,
                ),
            ),
        )

        assertThat(extras.getString(Constants.SIMPRINTS_SESSION_ID)).isEqualTo("sessionId")
        assertThat(extras.getString(Constants.SIMPRINTS_VERIFICATION)).isEqualTo(
            """
            {"guid":"guid","confidenceBand":"HIGH","confidence":50,"isSuccess":true}
            """.trimIndent(),
        )
        assertThat(extras.getBoolean(Constants.SIMPRINTS_BIOMETRICS_COMPLETE_CHECK)).isTrue()
    }

    @Test
    fun `correctly maps verify response without success after LibSimprints refactoring`() {
        val extras = mapper(
            ActionResponse.VerifyActionResponse(
                actionIdentifier = VerifyActionFactory
                    .getIdentifier()
                    .copy(contractVersion = VersionsList.INITIAL_REWORK),
                sessionId = "sessionId",
                matchResult = AppMatchResult(
                    guid = "guid",
                    confidenceScore = 50,
                    matchConfidence = AppMatchConfidence.HIGH,
                    verificationSuccess = null,
                ),
            ),
        )

        assertThat(extras.getString(Constants.SIMPRINTS_SESSION_ID)).isEqualTo("sessionId")
        assertThat(extras.getString(Constants.SIMPRINTS_VERIFICATION)).isEqualTo(
            """
            {"guid":"guid","confidenceBand":"HIGH","confidence":50}
            """.trimIndent(),
        )
        assertThat(extras.getBoolean(Constants.SIMPRINTS_BIOMETRICS_COMPLETE_CHECK)).isTrue()
    }

    @Test
    fun `correctly maps exit form response`() {
        val extras = mapper(
            ActionResponse.ExitFormActionResponse(
                actionIdentifier = EnrolLastBiometricsActionFactory.getIdentifier(),
                sessionId = "sessionId",
                reason = "reason",
                extraText = "extra",
            ),
        )

        assertThat(extras.getString(Constants.SIMPRINTS_SESSION_ID)).isEqualTo("sessionId")
        assertThat(extras.getParcelable<LegacyRefusalForm>(Constants.SIMPRINTS_REFUSAL_FORM)).isEqualTo(
            LegacyRefusalForm("reason", "extra"),
        )
        assertThat(extras.getBoolean(Constants.SIMPRINTS_BIOMETRICS_COMPLETE_CHECK)).isTrue()
    }

    @Test
    fun `correctly maps exit form response after LibSimprints refactoring`() {
        val extras = mapper(
            ActionResponse.ExitFormActionResponse(
                actionIdentifier = EnrolLastBiometricsActionFactory
                    .getIdentifier()
                    .copy(contractVersion = VersionsList.INITIAL_REWORK),
                sessionId = "sessionId",
                reason = "reason",
                extraText = "extra",
            ),
        )

        assertThat(extras.getString(Constants.SIMPRINTS_SESSION_ID)).isEqualTo("sessionId")
        assertThat(extras.getString(Constants.SIMPRINTS_REFUSAL_FORM)).isEqualTo(
            """
            {"reason":"reason","extra":"extra"}
            """.trimIndent(),
        )
        assertThat(extras.getBoolean(Constants.SIMPRINTS_BIOMETRICS_COMPLETE_CHECK)).isTrue()
    }

    @Test
    fun `correctly maps error response`() {
        val extras = mapper(
            ActionResponse.ErrorActionResponse(
                actionIdentifier = EnrolActionFactory.getIdentifier(),
                sessionId = "sessionId",
                reason = AppErrorReason.UNEXPECTED_ERROR,
                flowCompleted = true,
            ),
        )

        assertThat(extras.getString(Constants.SIMPRINTS_SESSION_ID)).isEqualTo("sessionId")
        assertThat(extras.getBoolean(Constants.SIMPRINTS_BIOMETRICS_COMPLETE_CHECK)).isTrue()
        assertThat(extras.getInt(LibSimprintsResponseMapper.RESULT_CODE_OVERRIDE)).isEqualTo(
            Constants.SIMPRINTS_UNEXPECTED_ERROR,
        )
    }

    @Test
    fun `correctly maps error code override`() {
        mapOf(
            AppErrorReason.UNEXPECTED_ERROR to Constants.SIMPRINTS_UNEXPECTED_ERROR,
            AppErrorReason.ROOTED_DEVICE to Constants.SIMPRINTS_ROOTED_DEVICE,
            AppErrorReason.LOGIN_NOT_COMPLETE to Constants.SIMPRINTS_LOGIN_NOT_COMPLETE,
            AppErrorReason.DIFFERENT_PROJECT_ID_SIGNED_IN to Constants.SIMPRINTS_INVALID_PROJECT_ID,
            AppErrorReason.DIFFERENT_USER_ID_SIGNED_IN to Constants.SIMPRINTS_INVALID_USER_ID,
            AppErrorReason.GUID_NOT_FOUND_ONLINE to Constants.SIMPRINTS_VERIFY_GUID_NOT_FOUND_ONLINE,
            AppErrorReason.GUID_NOT_FOUND_OFFLINE to Constants.SIMPRINTS_VERIFY_GUID_NOT_FOUND_OFFLINE,
            AppErrorReason.ENROLMENT_LAST_BIOMETRICS_FAILED to Constants.SIMPRINTS_ENROLMENT_LAST_BIOMETRICS_FAILED,
            AppErrorReason.BLUETOOTH_NOT_SUPPORTED to Constants.SIMPRINTS_BLUETOOTH_NOT_SUPPORTED,
            AppErrorReason.BLUETOOTH_NO_PERMISSION to Constants.SIMPRINTS_BLUETOOTH_NO_PERMISSION,
            AppErrorReason.FINGERPRINT_CONFIGURATION_ERROR to Constants.SIMPRINTS_FINGERPRINT_CONFIGURATION_ERROR,
            AppErrorReason.FACE_CONFIGURATION_ERROR to Constants.SIMPRINTS_FACE_CONFIGURATION_ERROR,
            AppErrorReason.LICENSE_MISSING to Constants.SIMPRINTS_LICENSE_MISSING,
            AppErrorReason.LICENSE_INVALID to Constants.SIMPRINTS_LICENSE_INVALID,
            AppErrorReason.BACKEND_MAINTENANCE_ERROR to Constants.SIMPRINTS_BACKEND_MAINTENANCE_ERROR,
            AppErrorReason.PROJECT_PAUSED to Constants.SIMPRINTS_PROJECT_PAUSED,
            AppErrorReason.PROJECT_ENDING to Constants.SIMPRINTS_PROJECT_ENDING,
            AppErrorReason.AGE_GROUP_NOT_SUPPORTED to Constants.SIMPRINTS_AGE_GROUP_NOT_SUPPORTED,
        ).forEach { (reason, expectedCode) ->
            val extras = mapper(
                ActionResponse.ErrorActionResponse(
                    actionIdentifier = EnrolActionFactory.getIdentifier(),
                    sessionId = "sessionId",
                    reason = reason,
                    flowCompleted = true,
                ),
            )

            assertThat(extras.getInt(LibSimprintsResponseMapper.RESULT_CODE_OVERRIDE)).isEqualTo(
                expectedCode,
            )
        }
    }
}
