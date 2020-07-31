package com.simprints.id.data.db.event.remote

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.simprints.id.commontesttools.sessionEvents.*
import com.simprints.id.data.db.event.local.models.*
import com.simprints.id.data.db.event.remote.models.ApiEventPayloadType
import com.simprints.id.data.db.event.remote.models.ApiEventPayloadType.*
import com.simprints.id.data.db.event.remote.models.fromApiToDomain
import com.simprints.id.data.db.event.remote.models.fromDomainToApi
import com.simprints.id.tools.json.SimJsonHelper
import org.json.JSONObject
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ApiEventTest {

    private val jackson = SimJsonHelper.jackson

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
    fun validate_enrolmentEventApiModel() {
        val event = createEnrolmentEvent()
        val apiEvent = event.fromDomainToApi()
        val json = JSONObject(jackson.writeValueAsString(apiEvent))

        validateEnrolmentEventApiModel(json)
    }

    @Test
    fun validate_EnrolmentRecordCreationEventApiModel() {
        val event = createEnrolmentRecordCreationEvent()
        val apiEvent = event.fromDomainToApi()
        val json = JSONObject(jackson.writeValueAsString(apiEvent))

        validateEnrolmentRecordCreationEventApiModel(json)
        assertThat(event).isEqualTo(apiEvent.fromApiToDomain())
    }

    @Test
    fun validate_EnrolmentRecordMoveEventApiModel() {
        val event = createEnrolmentRecordMoveEvent()
        val apiEvent = event.fromDomainToApi()
        val json = JSONObject(jackson.writeValueAsString(apiEvent))

        validateEnrolmentRecordMoveEventApiModel(json)
        assertThat(event).isEqualTo(apiEvent.fromApiToDomain())
    }

    @Test
    fun validate_EnrolmentRecordDeletionEventApiModel() {
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
    fun validate_ScannerFirmwareUpdateEvent() {
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
    fun validate_FaceCaptureRetryEventApiModel() {
        val event = createFaceCaptureRetryEvent()
        val apiEvent = event.fromDomainToApi()
        val json = JSONObject(jackson.writeValueAsString(apiEvent))

        validateFaceCaptureRetryEventApiModel(json)
    }


    // Never invoked, but used to enforce that every test class has a test
    fun enforceThatAnyTestHasATest() {
        val type: ApiEventPayloadType? = null
        when (type) {
            ENROLMENT_RECORD_CREATION -> validate_EnrolmentRecordCreationEventApiModel()
            ENROLMENT_RECORD_DELETION -> validate_EnrolmentRecordDeletionEventApiModel()
            ENROLMENT_RECORD_MOVE -> validate_EnrolmentRecordMoveEventApiModel()
            CALLOUT -> Throwable("Callout has multiple version - there is a version for each")
            CALLBACK -> Throwable("Callback has multiple version - there is a version for each")
            ARTIFICIAL_TERMINATION -> validate_artificialTerminationEventApiModel()
            AUTHENTICATION -> validate_authenticationEventApiModel()
            CONSENT -> validate_consentEventApiModel()
            ENROLMENT -> validate_enrolmentEventApiModel()
            AUTHORIZATION -> validate_authorizationEventApiModel()
            FINGERPRINT_CAPTURE -> validate_fingerprintCaptureEventApiModel()
            ONE_TO_ONE_MATCH -> validate_oneToOneMatchEventApiModel()
            ONE_TO_MANY_MATCH -> validate_oneToManyMatchEventApiModel()
            PERSON_CREATION -> validate_personCreationEventApiModel()
            ALERT_SCREEN -> validate_alertScreenEventApiModel()
            GUID_SELECTION -> validate_guidSelectionEventApiModel()
            CONNECTIVITY_SNAPSHOT -> validate_connectivitySnapshotEventApiModel()
            REFUSAL -> validate_refusalEventApiModel()
            CANDIDATE_READ -> validate_candidateReadEventApiModel()
            SCANNER_CONNECTION -> validate_scannerConnectionEventApiModel()
            VERO_2_INFO_SNAPSHOT -> validate_vero2InfoSnapshotEvent()
            SCANNER_FIRMWARE_UPDATE -> validate_ScannerFirmwareUpdateEvent()
            INVALID_INTENT -> validate_IntentParsingEventApiModel()
            SUSPICIOUS_INTENT -> validate_suspiciousIntentEventApiModel()
            INTENT_PARSING -> validate_IntentParsingEventApiModel()
            COMPLETION_CHECK -> validate_completionCheckEventApiModel()
            SESSION_CAPTURE -> validate_sessionCaptureEventApiModel()
            FACE_ONBOARDING_COMPLETE -> validate_FaceOnboardingCompleteEventApiModel()
            FACE_FALLBACK_CAPTURE -> validate_FaceFallbackCaptureEventApiModel()
            FACE_CAPTURE -> validate_FaceCaptureEventApiModel()
            FACE_CAPTURE_CONFIRMATION -> validate_FaceCaptureConfirmationEventApiModel()
            FACE_CAPTURE_RETRY -> validate_FaceCaptureRetryEventApiModel()
            null -> {
            }
        }
    }
}
