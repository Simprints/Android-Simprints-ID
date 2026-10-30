package com.simprints.feature.clientapi.mappers.response

import androidx.core.os.bundleOf
import androidx.test.ext.junit.runners.*
import com.google.common.truth.Truth.*
import com.simprints.core.domain.externalcredential.ExternalCredentialType
import com.simprints.core.domain.response.AppErrorReason
import com.simprints.core.domain.response.AppMatchConfidence
import com.simprints.core.domain.tokenization.asTokenizableRaw
import com.simprints.feature.clientapi.mappers.request.requestFactories.ConfirmIdentityActionFactory
import com.simprints.feature.clientapi.mappers.request.requestFactories.EnrolActionFactory
import com.simprints.feature.clientapi.mappers.request.requestFactories.EnrolLastBiometricsActionFactory
import com.simprints.feature.clientapi.mappers.request.requestFactories.IdentifyRequestActionFactory
import com.simprints.feature.clientapi.mappers.request.requestFactories.VerifyActionFactory
import com.simprints.feature.clientapi.mappers.response.CommCareResponseMapper.Companion.SCANNED_CREDENTIAL_DOCUMENT_FIELDS
import com.simprints.feature.clientapi.models.CommCareConstants
import com.simprints.infra.orchestration.data.ActionResponse
import com.simprints.infra.orchestration.data.responses.AppExternalCredential
import com.simprints.infra.orchestration.data.responses.AppMatchResult
import com.simprints.libsimprints.Constants
import com.simprints.libsimprints.contracts.VersionsList
import org.junit.Test
import org.junit.runner.RunWith
import com.simprints.libsimprints.Identification as LegacyIdentification
import com.simprints.libsimprints.Tier as LegacyTier

@RunWith(AndroidJUnit4::class)
class CommCareResponseMapperTest {
    private val mapper = CommCareResponseMapper("deviceId", "appVersionName")

    private val credential = AppExternalCredential(
        id = "credentialId",
        value = "credential-value".asTokenizableRaw(),
        type = ExternalCredentialType.NHISCard,
        nonCredentialFields = mapOf("name" to "John"),
    )

    @Test
    fun `correctly maps enrol response`() {
        val extras = mapper(
            ActionResponse.EnrolActionResponse(
                actionIdentifier = EnrolActionFactory.getIdentifier(),
                sessionId = "sessionId",
                enrolledGuid = "guid",
                subjectActions = "subjects",
                externalCredential = null,
            ),
        ).getBundle(CommCareConstants.COMMCARE_BUNDLE_KEY) ?: bundleOf()

        assertThat(extras.getString(CommCareConstants.SIMPRINTS_SESSION_ID)).isEqualTo("sessionId")
        assertThat(extras.getString(CommCareConstants.COMMCARE_DEVICE_ID)).isEqualTo("deviceId")
        assertThat(extras.getString(CommCareConstants.COMMCARE_SID_VERSION)).isEqualTo("appVersionName")
        assertThat(extras.getString(CommCareConstants.REGISTRATION_GUID_KEY)).isEqualTo("guid")
        assertThat(extras.getString(CommCareConstants.BIOMETRICS_COMPLETE_CHECK_KEY)).isEqualTo("true")
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
                isMultiFactorIdEnabled = false,
                scannedCredential = null,
            ),
        )

        assertThat(extras.getString(Constants.SIMPRINTS_SESSION_ID)).isEqualTo("sessionId")
        assertThat(extras.getString(CommCareConstants.COMMCARE_DEVICE_ID)).isEqualTo("deviceId")
        assertThat(extras.getString(CommCareConstants.COMMCARE_SID_VERSION)).isEqualTo("appVersionName")
        @Suppress("DEPRECATION")
        // Intentionally using deprecated getParcelableArrayList() as this is what CommCare uses
        assertThat(extras.getParcelableArrayList<LegacyIdentification>(Constants.SIMPRINTS_IDENTIFICATIONS))
            .isEqualTo(
                ArrayList<LegacyIdentification>(
                    listOf(
                        LegacyIdentification("guid-1", 100, LegacyTier.TIER_2),
                        LegacyIdentification("guid-2", 75, LegacyTier.TIER_3),
                    ),
                ),
            )
    }

    @Test
    fun `maps identify response in legacy format for versions before external credentials`() {
        val extras = mapper(
            ActionResponse.IdentifyActionResponse(
                actionIdentifier = IdentifyRequestActionFactory
                    .getIdentifier()
                    .copy(contractVersion = VersionsList.ADDED_EXTERNAL_CREDENTIALS - 1),
                sessionId = "sessionId",
                identifications = listOf(
                    AppMatchResult(
                        guid = "guid-1",
                        confidenceScore = 100,
                        matchConfidence = AppMatchConfidence.MEDIUM,
                        isLinkedToScannedCredential = true,
                        isCredentialVerified = true,
                    ),
                ),
                isMultiFactorIdEnabled = true,
                scannedCredential = credential,
            ),
        )

        @Suppress("DEPRECATION")
        assertThat(extras.getParcelableArrayList<LegacyIdentification>(Constants.SIMPRINTS_IDENTIFICATIONS))
            .containsExactly(LegacyIdentification("guid-1", 100, LegacyTier.TIER_2))
        assertThat(extras.containsKey(Constants.SIMPRINTS_SCANNED_CREDENTIAL)).isFalse()
        assertThat(extras.containsKey(Constants.SIMPRINTS_BIOMETRICS_COMPLETE_CHECK)).isFalse()
    }

    @Test
    fun `maps identify response with multi-factor ID data for external credentials version`() {
        val extras = mapper(
            ActionResponse.IdentifyActionResponse(
                actionIdentifier = IdentifyRequestActionFactory
                    .getIdentifier()
                    .copy(contractVersion = VersionsList.ADDED_EXTERNAL_CREDENTIALS),
                sessionId = "sessionId",
                identifications = listOf(
                    AppMatchResult(
                        guid = "guid-1",
                        confidenceScore = 100,
                        matchConfidence = AppMatchConfidence.HIGH,
                        isLinkedToScannedCredential = true,
                        isCredentialVerified = true,
                    ),
                    AppMatchResult(
                        guid = "guid-2",
                        confidenceScore = 75,
                        matchConfidence = AppMatchConfidence.LOW,
                    ),
                ),
                isMultiFactorIdEnabled = true,
                scannedCredential = credential,
            ),
        )

        assertThat(extras.containsKey(CommCareConstants.COMMCARE_DATA_KEY)).isFalse()
        assertThat(extras.getString(Constants.SIMPRINTS_SESSION_ID)).isEqualTo("sessionId")
        assertThat(extras.getString(CommCareConstants.COMMCARE_DEVICE_ID)).isEqualTo("deviceId")
        assertThat(extras.getString(CommCareConstants.COMMCARE_SID_VERSION)).isEqualTo("appVersionName")
        assertThat(extras.getString(Constants.SIMPRINTS_BIOMETRICS_COMPLETE_CHECK)).isEqualTo("true")
        assertThat(extras.getString(Constants.SIMPRINTS_IDENTIFICATIONS)).isEqualTo(
            """
            [{"guid":"guid-1","confidenceBand":"HIGH","confidence":100,"isLinkedToCredential":true,"isVerified":true},{"guid":"guid-2","confidenceBand":"LOW","confidence":75,"isLinkedToCredential":false}]
            """.trimIndent(),
        )
        assertThat(extras.getString(Constants.SIMPRINTS_SCANNED_CREDENTIAL)).isEqualTo(
            """{"type":"NHISCard","value":"credential-value","$SCANNED_CREDENTIAL_DOCUMENT_FIELDS":{"name":"John"}}""",
        )
    }

    @Test
    fun `maps identify response without multi-factor ID data when it is disabled`() {
        val extras = mapper(
            ActionResponse.IdentifyActionResponse(
                actionIdentifier = IdentifyRequestActionFactory
                    .getIdentifier()
                    .copy(contractVersion = VersionsList.ADDED_EXTERNAL_CREDENTIALS),
                sessionId = "sessionId",
                identifications = listOf(
                    AppMatchResult(
                        guid = "guid-1",
                        confidenceScore = 100,
                        matchConfidence = AppMatchConfidence.MEDIUM,
                    ),
                ),
                isMultiFactorIdEnabled = false,
                scannedCredential = credential,
            ),
        )

        assertThat(extras.getString(Constants.SIMPRINTS_IDENTIFICATIONS)).isEqualTo(
            """
            [{"guid":"guid-1","confidenceBand":"MEDIUM","confidence":100}]
            """.trimIndent(),
        )
        assertThat(extras.containsKey(Constants.SIMPRINTS_SCANNED_CREDENTIAL)).isFalse()
    }

    @Test
    fun `correctly maps confirm response`() {
        val extras = mapper(
            ActionResponse.ConfirmActionResponse(
                actionIdentifier = ConfirmIdentityActionFactory.getIdentifier(),
                sessionId = "sessionId",
                confirmed = true,
                subjectActions = null,
                externalCredential = null,
            ),
        ).getBundle(CommCareConstants.COMMCARE_BUNDLE_KEY) ?: bundleOf()

        assertThat(extras.getString(CommCareConstants.SIMPRINTS_SESSION_ID)).isEqualTo("sessionId")
        assertThat(extras.getString(CommCareConstants.COMMCARE_DEVICE_ID)).isEqualTo("deviceId")
        assertThat(extras.getString(CommCareConstants.COMMCARE_SID_VERSION)).isEqualTo("appVersionName")
        assertThat(extras.getString(CommCareConstants.BIOMETRICS_COMPLETE_CHECK_KEY)).isEqualTo("true")
    }

    @Test
    fun `correctly maps confirm response with cosync data`() {
        val extras = mapper(
            ActionResponse.ConfirmActionResponse(
                actionIdentifier = ConfirmIdentityActionFactory.getIdentifier(),
                sessionId = "sessionId",
                confirmed = true,
                subjectActions = "cosyncJson",
                externalCredential = null,
            ),
        ).getBundle(CommCareConstants.COMMCARE_BUNDLE_KEY) ?: bundleOf()

        assertThat(extras.getString(Constants.SIMPRINTS_COSYNC_SUBJECT_ACTIONS)).isEqualTo("cosyncJson")
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
        ).getBundle(CommCareConstants.COMMCARE_BUNDLE_KEY) ?: bundleOf()

        assertThat(extras.getString(CommCareConstants.SIMPRINTS_SESSION_ID)).isEqualTo("sessionId")
        assertThat(extras.getString(CommCareConstants.COMMCARE_DEVICE_ID)).isEqualTo("deviceId")
        assertThat(extras.getString(CommCareConstants.COMMCARE_SID_VERSION)).isEqualTo("appVersionName")
        assertThat(extras.getString(CommCareConstants.VERIFICATION_GUID_KEY)).isEqualTo("guid")
        assertThat(extras.getString(CommCareConstants.VERIFICATION_CONFIDENCE_KEY)).isEqualTo("50")
        assertThat(extras.getString(CommCareConstants.VERIFICATION_TIER_KEY)).isEqualTo("TIER_1")
        assertThat(extras.getString(CommCareConstants.BIOMETRICS_COMPLETE_CHECK_KEY)).isEqualTo("true")
        assertThat(extras.getString(CommCareConstants.VERIFICATION_SUCCESS_KEY)).isNull()
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
        ).getBundle(CommCareConstants.COMMCARE_BUNDLE_KEY) ?: bundleOf()

        assertThat(extras.getString(CommCareConstants.SIMPRINTS_SESSION_ID)).isEqualTo("sessionId")
        assertThat(extras.getString(CommCareConstants.COMMCARE_DEVICE_ID)).isEqualTo("deviceId")
        assertThat(extras.getString(CommCareConstants.COMMCARE_SID_VERSION)).isEqualTo("appVersionName")
        assertThat(extras.getString(CommCareConstants.VERIFICATION_GUID_KEY)).isEqualTo("guid")
        assertThat(extras.getString(CommCareConstants.VERIFICATION_CONFIDENCE_KEY)).isEqualTo("50")
        assertThat(extras.getString(CommCareConstants.VERIFICATION_TIER_KEY)).isEqualTo("TIER_1")
        assertThat(extras.getString(CommCareConstants.BIOMETRICS_COMPLETE_CHECK_KEY)).isEqualTo("true")
        assertThat(extras.getString(CommCareConstants.VERIFICATION_SUCCESS_KEY)).isEqualTo("false")
    }

    @Test
    fun `correctly maps verify response with verificationSuccess = true`() {
        val extras = mapper(
            ActionResponse.VerifyActionResponse(
                actionIdentifier = VerifyActionFactory.getIdentifier(),
                sessionId = "sessionId",
                matchResult = AppMatchResult(
                    guid = "guid",
                    confidenceScore = 50,
                    matchConfidence = AppMatchConfidence.HIGH,
                    verificationSuccess = true,
                ),
            ),
        ).getBundle(CommCareConstants.COMMCARE_BUNDLE_KEY) ?: bundleOf()

        assertThat(extras.getString(CommCareConstants.SIMPRINTS_SESSION_ID)).isEqualTo("sessionId")
        assertThat(extras.getString(CommCareConstants.COMMCARE_DEVICE_ID)).isEqualTo("deviceId")
        assertThat(extras.getString(CommCareConstants.COMMCARE_SID_VERSION)).isEqualTo("appVersionName")
        assertThat(extras.getString(CommCareConstants.VERIFICATION_GUID_KEY)).isEqualTo("guid")
        assertThat(extras.getString(CommCareConstants.VERIFICATION_CONFIDENCE_KEY)).isEqualTo("50")
        assertThat(extras.getString(CommCareConstants.VERIFICATION_TIER_KEY)).isEqualTo("TIER_1")
        assertThat(extras.getString(CommCareConstants.BIOMETRICS_COMPLETE_CHECK_KEY)).isEqualTo("true")
        assertThat(extras.getString(CommCareConstants.VERIFICATION_SUCCESS_KEY)).isEqualTo("true")
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
        ).getBundle(CommCareConstants.COMMCARE_BUNDLE_KEY) ?: bundleOf()

        assertThat(extras.getString(CommCareConstants.SIMPRINTS_SESSION_ID)).isEqualTo("sessionId")
        assertThat(extras.getString(CommCareConstants.EXIT_REASON)).isEqualTo("reason")
        assertThat(extras.getString(CommCareConstants.EXIT_EXTRA)).isEqualTo("extra")
        assertThat(extras.getString(CommCareConstants.BIOMETRICS_COMPLETE_CHECK_KEY)).isEqualTo("true")
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
        ).getBundle(CommCareConstants.COMMCARE_BUNDLE_KEY) ?: bundleOf()

        assertThat(extras.getString(CommCareConstants.SIMPRINTS_SESSION_ID)).isEqualTo("sessionId")
        assertThat(extras.getString(CommCareConstants.COMMCARE_DEVICE_ID)).isEqualTo("deviceId")
        assertThat(extras.getString(CommCareConstants.COMMCARE_SID_VERSION)).isEqualTo("appVersionName")
        assertThat(extras.getString(CommCareConstants.BIOMETRICS_COMPLETE_CHECK_KEY)).isEqualTo("true")
    }
}
