package com.simprints.infra.eventsync.event.remote

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.simprints.core.domain.tokenization.TokenizableString
import com.simprints.core.domain.tokenization.asTokenizableEncrypted
import com.simprints.core.domain.tokenization.asTokenizableRaw
import com.simprints.core.tools.extentions.safeSealedWhens
import com.simprints.core.tools.json.JsonHelper
import com.simprints.infra.events.*
import com.simprints.infra.events.sampledata.*
import com.simprints.infra.eventsync.event.*
import com.simprints.infra.eventsync.event.remote.models.ApiEventPayloadType
import com.simprints.infra.eventsync.event.remote.models.ApiEventPayloadType.*
import com.simprints.infra.eventsync.event.remote.models.fromDomainToApi
import org.json.JSONObject
import org.junit.Test
import org.junit.runner.RunWith

@Suppress("IMPLICIT_CAST_TO_ANY")
@RunWith(AndroidJUnit4::class)
class ApiEventTest {

    private val jackson = JsonHelper.jackson

    @Test
    fun validate_alertScreenEventApiModel() {
        val event = createAlertScreenEvent()
        val apiEvent = event.fromDomainToApi()
        val json = JSONObject(jackson.writeValueAsString(apiEvent))

        validateAlertScreenEventApiModel(json)
    }

    @Test
    fun validate_artificialTerminationEventApiModel() {
        val event = createArtificialTerminationEvent()
        val apiEvent = event.fromDomainToApi()
        val json = JSONObject(jackson.writeValueAsString(apiEvent))

        validateArtificialTerminationEventApiModel(json)
    }

    @Test
    fun validate_IntentParsingEventApiModel() {
        val event = createIntentParsingEvent()
        val apiEvent = event.fromDomainToApi()
        val json = JSONObject(jackson.writeValueAsString(apiEvent))

        validateIntentParsingEventApiModel(json)
    }

    @Test
    fun validate_authenticationEventApiModel() {
        val event = createAuthenticationEvent()
        val apiEvent = event.fromDomainToApi()
        val json = JSONObject(jackson.writeValueAsString(apiEvent))

        validateAuthenticationEventApiModel(json)
    }

    @Test
    fun validate_authorizationEventApiModel() {
        val event = createAuthorizationEvent()
        val apiEvent = event.fromDomainToApi()
        val json = JSONObject(jackson.writeValueAsString(apiEvent))

        validateAuthorizationEventApiModel(json)
    }

    @Test
    fun validate_calloutEventForVerificationApiModel() {
        val event = createVerificationCalloutEvent()
        val apiEvent = event.fromDomainToApi()
        val json = JSONObject(jackson.writeValueAsString(apiEvent))

        validateCalloutEventApiModel(json)
    }

    @Test
    fun validate_calloutEventForIdentificationApiModel() {
        val event = createIdentificationCalloutEvent()
        val apiEvent = event.fromDomainToApi()
        val json = JSONObject(jackson.writeValueAsString(apiEvent))

        validateCalloutEventApiModel(json)
    }

    @Test
    fun validate_calloutEventForEnrolLastBiometricsModel() {
        val event = createLastBiometricsEnrolmentCalloutEvent()
        val apiEvent = event.fromDomainToApi()
        val json = JSONObject(jackson.writeValueAsString(apiEvent))

        validateCalloutEventApiModel(json)
    }

    @Test
    fun validate_calloutEventForConfirmationApiModel() {
        val event = createConfirmationCalloutEvent()
        val apiEvent = event.fromDomainToApi()
        val json = JSONObject(jackson.writeValueAsString(apiEvent))

        validateCalloutEventApiModel(json)
    }

    @Test
    fun validate_calloutEventForEnrolmentApiModel() {
        val event = createEnrolmentCalloutEvent()
        val apiEvent = event.fromDomainToApi()
        val json = JSONObject(jackson.writeValueAsString(apiEvent))

        validateCalloutEventApiModel(json)
    }

    @Test
    fun validate_callbackEventForEnrolmentApiModel() {
        val event = createEnrolmentCallbackEvent()
        val apiEvent = event.fromDomainToApi()
        val json = JSONObject(jackson.writeValueAsString(apiEvent))

        validateCallbackV1EventApiModel(json)
    }

    @Test
    fun validate_callbackEventForErrorApiModel() {
        val event = createEnrolmentCallbackEvent()
        val apiEvent = event.fromDomainToApi()
        val json = JSONObject(jackson.writeValueAsString(apiEvent))

        validateCallbackV1EventApiModel(json)
    }

    @Test
    fun validate_callbackEventForIdentificationApiV1Model() {
        val event = createIdentificationCallbackEventV1()
        val apiEvent = event.fromDomainToApi()
        val json = JSONObject(jackson.writeValueAsString(apiEvent))

        validateCallbackV1EventApiModel(json)
    }

    @Test
    fun validate_callbackEventForIdentificationApiV2Model() {
        val event = createIdentificationCallbackEventV2()
        val apiEvent = event.fromDomainToApi()
        val json = JSONObject(jackson.writeValueAsString(apiEvent))

        validateCallbackV2EventApiModel(json)
    }

    @Test
    fun validate_callbackEventForVerificationV1ApiModel() {
        val event = createVerificationCallbackEventV1()
        val apiEvent = event.fromDomainToApi()
        val json = JSONObject(jackson.writeValueAsString(apiEvent))

        validateCallbackV1EventApiModel(json)
    }

    @Test
    fun validate_callbackEventForVerificationV2ApiModel() {
        val event = createVerificationCallbackEventV2()
        val apiEvent = event.fromDomainToApi()
        val json = JSONObject(jackson.writeValueAsString(apiEvent))

        validateCallbackV2EventApiModel(json)
    }

    @Test
    fun validate_callbackEventForRefusalApiModel() {
        val event = createRefusalCallbackEvent()
        val apiEvent = event.fromDomainToApi()
        val json = JSONObject(jackson.writeValueAsString(apiEvent))

        validateCallbackV1EventApiModel(json)
    }

    @Test
    fun validate_callbackEventForConfirmationApiModel() {
        val event = createConfirmationCallbackEvent()
        val apiEvent = event.fromDomainToApi()
        val json = JSONObject(jackson.writeValueAsString(apiEvent))

        validateCallbackV1EventApiModel(json)
    }

    @Test
    fun validate_candidateReadEventApiModel() {
        val event = createCandidateReadEvent()
        val apiEvent = event.fromDomainToApi()
        val json = JSONObject(jackson.writeValueAsString(apiEvent))

        validateCandidateReadEventApiModel(json)
    }

    @Test
    fun validate_connectivitySnapshotEventApiModel() {
        val event = createConnectivitySnapshotEvent()
        val apiEvent = event.fromDomainToApi()
        val json = JSONObject(jackson.writeValueAsString(apiEvent))

        validateConnectivitySnapshotEventApiModel(json)
    }

    @Test
    fun validate_consentEventApiModel() {
        val event = createConsentEvent()
        val apiEvent = event.fromDomainToApi()
        val json = JSONObject(jackson.writeValueAsString(apiEvent))

        validateConsentEventApiModel(json)
    }

    @Test
    fun validateEnrolmentV1_enrolmentEventApiModel() {
        val event = createEnrolmentEventV1()
        val apiEvent = event.fromDomainToApi()
        val json = JSONObject(jackson.writeValueAsString(apiEvent))

        validateEnrolmentEventV1ApiModel(json)
    }

    @Test
    fun validateEnrolmentV2_enrolmentEventApiModel() {
        val event = createEnrolmentEventV2()
        val apiEvent = event.fromDomainToApi()
        val json = JSONObject(jackson.writeValueAsString(apiEvent))

        validateEnrolmentEventV2ApiModel(json)
    }

    @Test
    fun validate_completionCheckEventApiModel() {
        val event = createCompletionCheckEvent()
        val apiEvent = event.fromDomainToApi()
        val json = JSONObject(jackson.writeValueAsString(apiEvent))

        validateCompletionCheckEventApiModel(json)
    }

    @Test
    fun validate_fingerprintCaptureEventApiModel() {
        val event = createFingerprintCaptureEvent()
        val apiEvent = event.fromDomainToApi()
        val json = JSONObject(jackson.writeValueAsString(apiEvent))

        validateFingerprintCaptureEventApiModel(json)
    }

    @Test
    fun validate_guidSelectionEventApiModel() {
        val event = createGuidSelectionEvent()
        val apiEvent = event.fromDomainToApi()
        val json = JSONObject(jackson.writeValueAsString(apiEvent))

        validateGuidSelectionEventApiModel(json)
    }

    @Test
    fun validate_oneToManyMatchEventApiModel() {
        val event = createOneToManyMatchEvent()
        val apiEvent = event.fromDomainToApi()
        val json = JSONObject(jackson.writeValueAsString(apiEvent))

        validateOneToManyMatchEventApiModel(json)
    }

    @Test
    fun validate_oneToOneMatchEventApiModel() {
        val event = createOneToOneMatchEvent()
        val apiEvent = event.fromDomainToApi()
        val json = JSONObject(jackson.writeValueAsString(apiEvent))

        validateOneToOneMatchEventApiModel(json)
    }

    @Test
    fun validate_personCreationEventApiModel() {
        val event = createPersonCreationEvent()
        val apiEvent = event.fromDomainToApi()
        val json = JSONObject(jackson.writeValueAsString(apiEvent))

        validatePersonCreationEvent(json)
    }

    @Test
    fun validate_refusalEventApiModel() {
        val event = createRefusalEvent()
        val apiEvent = event.fromDomainToApi()
        val json = JSONObject(jackson.writeValueAsString(apiEvent))

        validateRefusalEventApiModel(json)
    }

    @Test
    fun validate_sessionCaptureEventApiModel() {
        val event = createSessionCaptureEvent()
        val apiEvent = event.fromDomainToApi()
        val json = JSONObject(jackson.writeValueAsString(apiEvent))

        validateSessionCaptureApiModel(json)
    }

    @Test
    fun validate_suspiciousIntentEventApiModel() {
        val event = createSuspiciousIntentEvent()
        val apiEvent = event.fromDomainToApi()
        val json = JSONObject(jackson.writeValueAsString(apiEvent))

        validateSuspiciousIntentEventApiModel(json)
    }

    @Test
    fun validate_scannerConnectionEventApiModel() {
        val event = createScannerConnectionEvent()
        val apiEvent = event.fromDomainToApi()
        val json = JSONObject(jackson.writeValueAsString(apiEvent))

        validateScannerConnectionEventApiModel(json)
    }

    @Test
    fun validate_vero2InfoSnapshotEvent() {
        val event = createVero2InfoSnapshotEvent()
        val apiEvent = event.fromDomainToApi()
        val json = JSONObject(jackson.writeValueAsString(apiEvent))

        validateVero2InfoSnapshotEventApiModel(json)
    }

    @Test
    fun validate_scannerFirmwareUpdateEvent() {
        val event = createScannerFirmwareUpdateEvent()
        val apiEvent = event.fromDomainToApi()
        val json = JSONObject(jackson.writeValueAsString(apiEvent))

        validateScannerFirmwareUpdateEventApiModel(json)
    }

    @Test
    fun validate_invalidEventApiModel() {
        val event = createInvalidIntentEvent()
        val apiEvent = event.fromDomainToApi()
        val json = JSONObject(jackson.writeValueAsString(apiEvent))

        validateInvalidEventApiModel(json)
    }

    @Test
    fun validate_FaceOnboardingCompleteEventApiModel() {
        val event = createFaceOnboardingCompleteEvent()
        val apiEvent = event.fromDomainToApi()
        val json = JSONObject(jackson.writeValueAsString(apiEvent))

        validateFaceOnboardingCompleteEventApiModel(json)
    }

    @Test
    fun validate_FaceFallbackCaptureEventApiModel() {
        val event = createFaceFallbackCaptureEvent()
        val apiEvent = event.fromDomainToApi()
        val json = JSONObject(jackson.writeValueAsString(apiEvent))

        validateFaceFallbackCaptureEventApiModel(json)
    }

    @Test
    fun validate_FaceCaptureEventApiModel() {
        val event = createFaceCaptureEvent()
        val apiEvent = event.fromDomainToApi()
        val json = JSONObject(jackson.writeValueAsString(apiEvent))

        validateFaceCaptureEventApiModel(json)
    }

    @Test
    fun validate_FaceCaptureConfirmationEventApiModel() {
        val event = createFaceCaptureConfirmationEvent()
        val apiEvent = event.fromDomainToApi()
        val json = JSONObject(jackson.writeValueAsString(apiEvent))

        validateFaceCaptureConfirmationEventApiModel(json)
    }

    @Test
    fun validate_FaceCaptureBiometricsEventApiModel() {
        val event = createFaceCaptureBiometricsEvent()
        val apiEvent = event.fromDomainToApi()
        val json = JSONObject(jackson.writeValueAsString(apiEvent))

        validateFaceCaptureBiometricsEventApiModel(json)
    }

    @Test
    fun validate_FingerprintCaptureBiometricsEventApiModel() {
        val event = createFingerprintCaptureBiometricsEvent()
        val apiEvent = event.fromDomainToApi()
        val json = JSONObject(jackson.writeValueAsString(apiEvent))

        validateFingerprintCaptureBiometricsEventApiModel(json)
    }

    @Test
    fun `when event contains tokenized attendant id, then ApiEvent should contain tokenizedField`() {
        validateUserIdTokenization(attendantId = "attendantId".asTokenizableEncrypted())
    }

    @Test
    fun `when event contains raw attendant id, then tokenizedField in ApiEvent should be empty`() {
        validateUserIdTokenization(attendantId = "attendantId".asTokenizableRaw())
    }

    @Test
    fun `when event contains tokenized module id, then ApiEvent should contain tokenizedField`() {
        validateModuleIdTokenization(moduleId = "moduleId".asTokenizableEncrypted())
    }

    @Test
    fun `when event contains raw module id, then tokenizedField in ApiEvent should be empty`() {
        validateModuleIdTokenization(moduleId = "moduleId".asTokenizableRaw())
    }

    private fun validateModuleIdTokenization(moduleId: TokenizableString) {
        val event = createEnrolmentEventV2().let {
            it.copy(payload = it.payload.copy(moduleId = moduleId))
        }
        with(event.fromDomainToApi().tokenizedFields) {
            when (moduleId) {
                is TokenizableString.Raw -> assertThat(size).isEqualTo(0)
                is TokenizableString.Tokenized -> {
                    assertThat(first()).isEqualTo("moduleId")
                    assertThat(size).isEqualTo(1)
                }
            }
        }
    }
    private fun validateUserIdTokenization(attendantId: TokenizableString) {
        val event = createEnrolmentEventV2().let {
            it.copy(payload = it.payload.copy(attendantId = attendantId))
        }
        with(event.fromDomainToApi().tokenizedFields) {
            when (attendantId) {
                is TokenizableString.Raw -> assertThat(size).isEqualTo(0)
                is TokenizableString.Tokenized -> {
                    assertThat(first()).isEqualTo("attendantId")
                    assertThat(size).isEqualTo(1)
                }
            }
        }
    }

    // Never invoked, but used to enforce the implementation of a test for every event event class
    fun enforceThatAnyTestHasATest() {
        val type: ApiEventPayloadType? = null
        when (type) {
            Callout -> Throwable("Callout has multiple version - there is a version for each")
            Callback -> Throwable("Callback has multiple version - there is a version for each")
            ArtificialTermination -> validate_artificialTerminationEventApiModel()
            Authentication -> validate_authenticationEventApiModel()
            Consent -> validate_consentEventApiModel()
            Enrolment -> validateEnrolmentV2_enrolmentEventApiModel()
            Authorization -> validate_authorizationEventApiModel()
            FingerprintCapture -> validate_fingerprintCaptureEventApiModel()
            OneToOneMatch -> validate_oneToOneMatchEventApiModel()
            OneToManyMatch -> validate_oneToManyMatchEventApiModel()
            PersonCreation -> validate_personCreationEventApiModel()
            AlertScreen -> validate_alertScreenEventApiModel()
            GuidSelection -> validate_guidSelectionEventApiModel()
            ConnectivitySnapshot -> validate_connectivitySnapshotEventApiModel()
            Refusal -> validate_refusalEventApiModel()
            CandidateRead -> validate_candidateReadEventApiModel()
            ScannerConnection -> validate_scannerConnectionEventApiModel()
            Vero2InfoSnapshot -> validate_vero2InfoSnapshotEvent()
            ScannerFirmwareUpdate -> validate_scannerFirmwareUpdateEvent()
            InvalidIntent -> validate_IntentParsingEventApiModel()
            SuspiciousIntent -> validate_suspiciousIntentEventApiModel()
            IntentParsing -> validate_IntentParsingEventApiModel()
            CompletionCheck -> validate_completionCheckEventApiModel()
            SessionCapture -> validate_sessionCaptureEventApiModel()
            FaceOnboardingComplete -> validate_FaceOnboardingCompleteEventApiModel()
            FaceFallbackCapture -> validate_FaceFallbackCaptureEventApiModel()
            FaceCapture -> validate_FaceCaptureEventApiModel()
            FaceCaptureConfirmation -> validate_FaceCaptureConfirmationEventApiModel()
            FingerprintCaptureBiometrics -> validate_FingerprintCaptureBiometricsEventApiModel()
            FaceCaptureBiometrics -> validate_FaceCaptureBiometricsEventApiModel()
            null -> TODO()
        }.safeSealedWhens
    }
}
