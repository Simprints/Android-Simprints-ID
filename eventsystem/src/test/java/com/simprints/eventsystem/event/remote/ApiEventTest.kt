package com.simprints.eventsystem.event.remote

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.simprints.core.tools.extentions.safeSealedWhens
import com.simprints.core.tools.json.JsonHelper
import com.simprints.eventsystem.event.remote.models.ApiEventPayloadType
import com.simprints.eventsystem.event.remote.models.ApiEventPayloadType.AlertScreen
import com.simprints.eventsystem.event.remote.models.ApiEventPayloadType.ArtificialTermination
import com.simprints.eventsystem.event.remote.models.ApiEventPayloadType.Authentication
import com.simprints.eventsystem.event.remote.models.ApiEventPayloadType.Authorization
import com.simprints.eventsystem.event.remote.models.ApiEventPayloadType.Callback
import com.simprints.eventsystem.event.remote.models.ApiEventPayloadType.Callout
import com.simprints.eventsystem.event.remote.models.ApiEventPayloadType.CandidateRead
import com.simprints.eventsystem.event.remote.models.ApiEventPayloadType.CompletionCheck
import com.simprints.eventsystem.event.remote.models.ApiEventPayloadType.ConnectivitySnapshot
import com.simprints.eventsystem.event.remote.models.ApiEventPayloadType.Consent
import com.simprints.eventsystem.event.remote.models.ApiEventPayloadType.Enrolment
import com.simprints.eventsystem.event.remote.models.ApiEventPayloadType.EnrolmentRecordCreation
import com.simprints.eventsystem.event.remote.models.ApiEventPayloadType.EnrolmentRecordDeletion
import com.simprints.eventsystem.event.remote.models.ApiEventPayloadType.EnrolmentRecordMove
import com.simprints.eventsystem.event.remote.models.ApiEventPayloadType.FaceCapture
import com.simprints.eventsystem.event.remote.models.ApiEventPayloadType.FaceCaptureBiometrics
import com.simprints.eventsystem.event.remote.models.ApiEventPayloadType.FaceCaptureConfirmation
import com.simprints.eventsystem.event.remote.models.ApiEventPayloadType.FaceFallbackCapture
import com.simprints.eventsystem.event.remote.models.ApiEventPayloadType.FaceOnboardingComplete
import com.simprints.eventsystem.event.remote.models.ApiEventPayloadType.FingerprintCapture
import com.simprints.eventsystem.event.remote.models.ApiEventPayloadType.FingerprintCaptureBiometrics
import com.simprints.eventsystem.event.remote.models.ApiEventPayloadType.GuidSelection
import com.simprints.eventsystem.event.remote.models.ApiEventPayloadType.IntentParsing
import com.simprints.eventsystem.event.remote.models.ApiEventPayloadType.InvalidIntent
import com.simprints.eventsystem.event.remote.models.ApiEventPayloadType.OneToManyMatch
import com.simprints.eventsystem.event.remote.models.ApiEventPayloadType.OneToOneMatch
import com.simprints.eventsystem.event.remote.models.ApiEventPayloadType.PersonCreation
import com.simprints.eventsystem.event.remote.models.ApiEventPayloadType.Refusal
import com.simprints.eventsystem.event.remote.models.ApiEventPayloadType.ScannerConnection
import com.simprints.eventsystem.event.remote.models.ApiEventPayloadType.ScannerFirmwareUpdate
import com.simprints.eventsystem.event.remote.models.ApiEventPayloadType.SessionCapture
import com.simprints.eventsystem.event.remote.models.ApiEventPayloadType.SuspiciousIntent
import com.simprints.eventsystem.event.remote.models.ApiEventPayloadType.Vero2InfoSnapshot
import com.simprints.eventsystem.event.remote.models.fromApiToDomain
import com.simprints.eventsystem.event.remote.models.fromDomainToApi
import com.simprints.eventsystem.sampledata.createAlertScreenEvent
import com.simprints.eventsystem.sampledata.createArtificialTerminationEvent
import com.simprints.eventsystem.sampledata.createAuthenticationEvent
import com.simprints.eventsystem.sampledata.createAuthorizationEvent
import com.simprints.eventsystem.sampledata.createCandidateReadEvent
import com.simprints.eventsystem.sampledata.createCompletionCheckEvent
import com.simprints.eventsystem.sampledata.createConfirmationCallbackEvent
import com.simprints.eventsystem.sampledata.createConfirmationCalloutEvent
import com.simprints.eventsystem.sampledata.createConnectivitySnapshotEvent
import com.simprints.eventsystem.sampledata.createConsentEvent
import com.simprints.eventsystem.sampledata.createEnrolmentCallbackEvent
import com.simprints.eventsystem.sampledata.createEnrolmentCalloutEvent
import com.simprints.eventsystem.sampledata.createEnrolmentEventV1
import com.simprints.eventsystem.sampledata.createEnrolmentEventV2
import com.simprints.eventsystem.sampledata.createEnrolmentRecordCreationEvent
import com.simprints.eventsystem.sampledata.createEnrolmentRecordDeletionEvent
import com.simprints.eventsystem.sampledata.createEnrolmentRecordMoveEvent
import com.simprints.eventsystem.sampledata.createFaceCaptureBiometricsEvent
import com.simprints.eventsystem.sampledata.createFaceCaptureConfirmationEvent
import com.simprints.eventsystem.sampledata.createFaceCaptureEventV3
import com.simprints.eventsystem.sampledata.createFaceFallbackCaptureEvent
import com.simprints.eventsystem.sampledata.createFaceOnboardingCompleteEvent
import com.simprints.eventsystem.sampledata.createFingerprintCaptureBiometricsEvent
import com.simprints.eventsystem.sampledata.createFingerprintCaptureEventV3
import com.simprints.eventsystem.sampledata.createGuidSelectionEvent
import com.simprints.eventsystem.sampledata.createIdentificationCallbackEvent
import com.simprints.eventsystem.sampledata.createIdentificationCalloutEvent
import com.simprints.eventsystem.sampledata.createIntentParsingEvent
import com.simprints.eventsystem.sampledata.createInvalidIntentEvent
import com.simprints.eventsystem.sampledata.createLastBiometricsEnrolmentCalloutEvent
import com.simprints.eventsystem.sampledata.createOneToManyMatchEvent
import com.simprints.eventsystem.sampledata.createOneToOneMatchEvent
import com.simprints.eventsystem.sampledata.createPersonCreationEvent
import com.simprints.eventsystem.sampledata.createRefusalCallbackEvent
import com.simprints.eventsystem.sampledata.createRefusalEvent
import com.simprints.eventsystem.sampledata.createScannerConnectionEvent
import com.simprints.eventsystem.sampledata.createScannerFirmwareUpdateEvent
import com.simprints.eventsystem.sampledata.createSessionCaptureEvent
import com.simprints.eventsystem.sampledata.createSuspiciousIntentEvent
import com.simprints.eventsystem.sampledata.createVerificationCallbackEvent
import com.simprints.eventsystem.sampledata.createVerificationCalloutEvent
import com.simprints.eventsystem.sampledata.createVero2InfoSnapshotEvent
import com.simprints.eventsystem.validateAlertScreenEventApiModel
import com.simprints.eventsystem.validateArtificialTerminationEventApiModel
import com.simprints.eventsystem.validateAuthenticationEventApiModel
import com.simprints.eventsystem.validateAuthorizationEventApiModel
import com.simprints.eventsystem.validateCallbackEventApiModel
import com.simprints.eventsystem.validateCalloutEventApiModel
import com.simprints.eventsystem.validateCandidateReadEventApiModel
import com.simprints.eventsystem.validateCompletionCheckEventApiModel
import com.simprints.eventsystem.validateConnectivitySnapshotEventApiModel
import com.simprints.eventsystem.validateConsentEventApiModel
import com.simprints.eventsystem.validateEnrolmentEventV1ApiModel
import com.simprints.eventsystem.validateEnrolmentEventV2ApiModel
import com.simprints.eventsystem.validateEnrolmentRecordCreationEventApiModel
import com.simprints.eventsystem.validateEnrolmentRecordDeletionEventApiModel
import com.simprints.eventsystem.validateEnrolmentRecordMoveEventApiModel
import com.simprints.eventsystem.validateFaceCaptureBiometricsEventApiModel
import com.simprints.eventsystem.validateFaceCaptureConfirmationEventApiModel
import com.simprints.eventsystem.validateFaceCaptureEventApiModel
import com.simprints.eventsystem.validateFaceFallbackCaptureEventApiModel
import com.simprints.eventsystem.validateFaceOnboardingCompleteEventApiModel
import com.simprints.eventsystem.validateFingerprintCaptureBiometricsEventApiModel
import com.simprints.eventsystem.validateFingerprintCaptureEventApiModel
import com.simprints.eventsystem.validateGuidSelectionEventApiModel
import com.simprints.eventsystem.validateIntentParsingEventApiModel
import com.simprints.eventsystem.validateInvalidEventApiModel
import com.simprints.eventsystem.validateOneToManyMatchEventApiModel
import com.simprints.eventsystem.validateOneToOneMatchEventApiModel
import com.simprints.eventsystem.validatePersonCreationEvent
import com.simprints.eventsystem.validateRefusalEventApiModel
import com.simprints.eventsystem.validateScannerConnectionEventApiModel
import com.simprints.eventsystem.validateScannerFirmwareUpdateEventApiModel
import com.simprints.eventsystem.validateSessionCaptureApiModel
import com.simprints.eventsystem.validateSuspiciousIntentEventApiModel
import com.simprints.eventsystem.validateVero2InfoSnapshotEventApiModel
import com.simprints.testtools.unit.EncodingUtilsImplForTests
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

        validateCallbackEventApiModel(json)
    }

    @Test
    fun validate_callbackEventForErrorApiModel() {
        val event = createEnrolmentCallbackEvent()
        val apiEvent = event.fromDomainToApi()
        val json = JSONObject(jackson.writeValueAsString(apiEvent))

        validateCallbackEventApiModel(json)
    }

    @Test
    fun validate_callbackEventForIdentificationApiModel() {
        val event = createIdentificationCallbackEvent()
        val apiEvent = event.fromDomainToApi()
        val json = JSONObject(jackson.writeValueAsString(apiEvent))

        validateCallbackEventApiModel(json)
    }

    @Test
    fun validate_callbackEventForVerificationApiModel() {
        val event = createVerificationCallbackEvent()
        val apiEvent = event.fromDomainToApi()
        val json = JSONObject(jackson.writeValueAsString(apiEvent))

        validateCallbackEventApiModel(json)
    }

    @Test
    fun validate_callbackEventForRefusalApiModel() {
        val event = createRefusalCallbackEvent()
        val apiEvent = event.fromDomainToApi()
        val json = JSONObject(jackson.writeValueAsString(apiEvent))

        validateCallbackEventApiModel(json)
    }

    @Test
    fun validate_callbackEventForConfirmationApiModel() {
        val event = createConfirmationCallbackEvent()
        val apiEvent = event.fromDomainToApi()
        val json = JSONObject(jackson.writeValueAsString(apiEvent))

        validateCallbackEventApiModel(json)
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
    fun validate_enrolmentRecordCreationEventApiModel() {
        val event = createEnrolmentRecordCreationEvent(EncodingUtilsImplForTests)
        val apiEvent = event.fromDomainToApi()
        val json = JSONObject(jackson.writeValueAsString(apiEvent))

        validateEnrolmentRecordCreationEventApiModel(json)
        assertThat(event).isEqualTo(apiEvent.fromApiToDomain())
    }

    @Test
    fun validate_enrolmentRecordMoveEventApiModel() {
        val event = createEnrolmentRecordMoveEvent(EncodingUtilsImplForTests)
        val apiEvent = event.fromDomainToApi()
        val json = JSONObject(jackson.writeValueAsString(apiEvent))

        validateEnrolmentRecordMoveEventApiModel(json)
        assertThat(event).isEqualTo(apiEvent.fromApiToDomain())
    }

    @Test
    fun validate_enrolmentRecordDeletionEventApiModel() {
        val event = createEnrolmentRecordDeletionEvent()
        val apiEvent = event.fromDomainToApi()
        val json = JSONObject(jackson.writeValueAsString(apiEvent))

        validateEnrolmentRecordDeletionEventApiModel(json)
        assertThat(event).isEqualTo(apiEvent.fromApiToDomain())
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
        val event = createFingerprintCaptureEventV3()
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
        val event = createFaceCaptureEventV3()
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

    // Never invoked, but used to enforce the implementation of a test for every event event class
    fun enforceThatAnyTestHasATest() {
        val type: ApiEventPayloadType? = null
        when (type) {
            EnrolmentRecordCreation -> validate_enrolmentRecordCreationEventApiModel()
            EnrolmentRecordDeletion -> validate_enrolmentRecordDeletionEventApiModel()
            EnrolmentRecordMove -> validate_enrolmentRecordMoveEventApiModel()
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
