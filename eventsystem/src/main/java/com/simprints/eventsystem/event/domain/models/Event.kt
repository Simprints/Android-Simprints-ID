package com.simprints.eventsystem.event.domain.models

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.simprints.eventsystem.event.domain.models.EventType.Companion.ALERT_SCREEN_KEY
import com.simprints.eventsystem.event.domain.models.EventType.Companion.ARTIFICIAL_TERMINATION_KEY
import com.simprints.eventsystem.event.domain.models.EventType.Companion.AUTHENTICATION_KEY
import com.simprints.eventsystem.event.domain.models.EventType.Companion.AUTHORIZATION_KEY
import com.simprints.eventsystem.event.domain.models.EventType.Companion.CALLBACK_CONFIRMATION_KEY
import com.simprints.eventsystem.event.domain.models.EventType.Companion.CALLBACK_ENROLMENT_KEY
import com.simprints.eventsystem.event.domain.models.EventType.Companion.CALLBACK_ERROR_KEY
import com.simprints.eventsystem.event.domain.models.EventType.Companion.CALLBACK_IDENTIFICATION_KEY
import com.simprints.eventsystem.event.domain.models.EventType.Companion.CALLBACK_REFUSAL_KEY
import com.simprints.eventsystem.event.domain.models.EventType.Companion.CALLBACK_VERIFICATION_KEY
import com.simprints.eventsystem.event.domain.models.EventType.Companion.CALLOUT_CONFIRMATION_KEY
import com.simprints.eventsystem.event.domain.models.EventType.Companion.CALLOUT_ENROLMENT_KEY
import com.simprints.eventsystem.event.domain.models.EventType.Companion.CALLOUT_IDENTIFICATION_KEY
import com.simprints.eventsystem.event.domain.models.EventType.Companion.CALLOUT_LAST_BIOMETRICS_KEY
import com.simprints.eventsystem.event.domain.models.EventType.Companion.CALLOUT_VERIFICATION_KEY
import com.simprints.eventsystem.event.domain.models.EventType.Companion.CANDIDATE_READ_KEY
import com.simprints.eventsystem.event.domain.models.EventType.Companion.COMPLETION_CHECK_KEY
import com.simprints.eventsystem.event.domain.models.EventType.Companion.CONNECTIVITY_SNAPSHOT_KEY
import com.simprints.eventsystem.event.domain.models.EventType.Companion.CONSENT_KEY
import com.simprints.eventsystem.event.domain.models.EventType.Companion.ENROLMENT_RECORD_CREATION_KEY
import com.simprints.eventsystem.event.domain.models.EventType.Companion.ENROLMENT_RECORD_DELETION_KEY
import com.simprints.eventsystem.event.domain.models.EventType.Companion.ENROLMENT_RECORD_MOVE_KEY
import com.simprints.eventsystem.event.domain.models.EventType.Companion.ENROLMENT_V1_KEY
import com.simprints.eventsystem.event.domain.models.EventType.Companion.ENROLMENT_V2_KEY
import com.simprints.eventsystem.event.domain.models.EventType.Companion.FACE_CAPTURE_BIOMETRICS_KEY
import com.simprints.eventsystem.event.domain.models.EventType.Companion.FACE_CAPTURE_CONFIRMATION_KEY
import com.simprints.eventsystem.event.domain.models.EventType.Companion.FACE_CAPTURE_V3_KEY
import com.simprints.eventsystem.event.domain.models.EventType.Companion.FACE_FALLBACK_CAPTURE_KEY
import com.simprints.eventsystem.event.domain.models.EventType.Companion.FACE_ONBOARDING_COMPLETE_KEY
import com.simprints.eventsystem.event.domain.models.EventType.Companion.FINGERPRINT_CAPTURE_BIOMETRICS_KEY
import com.simprints.eventsystem.event.domain.models.EventType.Companion.FINGERPRINT_CAPTURE_V3_KEY
import com.simprints.eventsystem.event.domain.models.EventType.Companion.GUID_SELECTION_KEY
import com.simprints.eventsystem.event.domain.models.EventType.Companion.INTENT_PARSING_KEY
import com.simprints.eventsystem.event.domain.models.EventType.Companion.INVALID_INTENT_KEY
import com.simprints.eventsystem.event.domain.models.EventType.Companion.ONE_TO_MANY_MATCH_KEY
import com.simprints.eventsystem.event.domain.models.EventType.Companion.ONE_TO_ONE_MATCH_KEY
import com.simprints.eventsystem.event.domain.models.EventType.Companion.PERSON_CREATION_KEY
import com.simprints.eventsystem.event.domain.models.EventType.Companion.REFUSAL_KEY
import com.simprints.eventsystem.event.domain.models.EventType.Companion.SCANNER_CONNECTION_KEY
import com.simprints.eventsystem.event.domain.models.EventType.Companion.SCANNER_FIRMWARE_UPDATE_KEY
import com.simprints.eventsystem.event.domain.models.EventType.Companion.SESSION_CAPTURE_KEY
import com.simprints.eventsystem.event.domain.models.EventType.Companion.SUSPICIOUS_INTENT_KEY
import com.simprints.eventsystem.event.domain.models.EventType.Companion.VERO_2_INFO_SNAPSHOT_KEY
import com.simprints.eventsystem.event.domain.models.callback.*
import com.simprints.eventsystem.event.domain.models.callout.*
import com.simprints.eventsystem.event.domain.models.face.FaceCaptureBiometricsEvent
import com.simprints.eventsystem.event.domain.models.face.FaceCaptureConfirmationEvent
import com.simprints.eventsystem.event.domain.models.face.FaceCaptureEvent
import com.simprints.eventsystem.event.domain.models.face.FaceFallbackCaptureEvent
import com.simprints.eventsystem.event.domain.models.face.FaceOnboardingCompleteEvent
import com.simprints.eventsystem.event.domain.models.fingerprint.FingerprintCaptureBiometricsEvent
import com.simprints.eventsystem.event.domain.models.fingerprint.FingerprintCaptureEventV3
import com.simprints.eventsystem.event.domain.models.session.SessionCaptureEvent
import com.simprints.eventsystem.event.domain.models.subject.EnrolmentRecordCreationEvent
import com.simprints.eventsystem.event.domain.models.subject.EnrolmentRecordDeletionEvent
import com.simprints.eventsystem.event.domain.models.subject.EnrolmentRecordMoveEvent


@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.EXISTING_PROPERTY,
    property = "type",
    visible = true
)
@JsonSubTypes(
    JsonSubTypes.Type(value = ConfirmationCallbackEvent::class, name = CALLBACK_CONFIRMATION_KEY),
    JsonSubTypes.Type(value = EnrolmentCallbackEvent::class, name = CALLBACK_ENROLMENT_KEY),
    JsonSubTypes.Type(value = ErrorCallbackEvent::class, name = CALLBACK_ERROR_KEY),
    JsonSubTypes.Type(
        value = IdentificationCallbackEvent::class,
        name = CALLBACK_IDENTIFICATION_KEY
    ),
    JsonSubTypes.Type(value = RefusalCallbackEvent::class, name = CALLBACK_REFUSAL_KEY),
    JsonSubTypes.Type(value = VerificationCallbackEvent::class, name = CALLBACK_VERIFICATION_KEY),
    JsonSubTypes.Type(value = ConfirmationCalloutEvent::class, name = CALLOUT_CONFIRMATION_KEY),
    JsonSubTypes.Type(value = EnrolmentCalloutEvent::class, name = CALLOUT_ENROLMENT_KEY),
    JsonSubTypes.Type(
        value = EnrolmentLastBiometricsCalloutEvent::class,
        name = CALLOUT_LAST_BIOMETRICS_KEY
    ),
    JsonSubTypes.Type(value = IdentificationCalloutEvent::class, name = CALLOUT_IDENTIFICATION_KEY),
    JsonSubTypes.Type(value = VerificationCalloutEvent::class, name = CALLOUT_VERIFICATION_KEY),
    JsonSubTypes.Type(
        value = FaceCaptureConfirmationEvent::class,
        name = FACE_CAPTURE_CONFIRMATION_KEY
    ),
    JsonSubTypes.Type(value = FaceCaptureEvent::class, name = FACE_CAPTURE_V3_KEY),
    JsonSubTypes.Type(value = FaceCaptureBiometricsEvent::class, name = FACE_CAPTURE_BIOMETRICS_KEY),
    JsonSubTypes.Type(value = FaceFallbackCaptureEvent::class, name = FACE_FALLBACK_CAPTURE_KEY),
    JsonSubTypes.Type(
        value = FaceOnboardingCompleteEvent::class,
        name = FACE_ONBOARDING_COMPLETE_KEY
    ),
    JsonSubTypes.Type(value = SessionCaptureEvent::class, name = SESSION_CAPTURE_KEY),
    JsonSubTypes.Type(
        value = EnrolmentRecordCreationEvent::class,
        name = ENROLMENT_RECORD_CREATION_KEY
    ),
    JsonSubTypes.Type(
        value = EnrolmentRecordDeletionEvent::class,
        name = ENROLMENT_RECORD_DELETION_KEY
    ),
    JsonSubTypes.Type(value = EnrolmentRecordMoveEvent::class, name = ENROLMENT_RECORD_MOVE_KEY),
    JsonSubTypes.Type(value = AlertScreenEvent::class, name = ALERT_SCREEN_KEY),
    JsonSubTypes.Type(value = ArtificialTerminationEvent::class, name = ARTIFICIAL_TERMINATION_KEY),
    JsonSubTypes.Type(value = AuthenticationEvent::class, name = AUTHENTICATION_KEY),
    JsonSubTypes.Type(value = AuthorizationEvent::class, name = AUTHORIZATION_KEY),
    JsonSubTypes.Type(value = CandidateReadEvent::class, name = CANDIDATE_READ_KEY),
    JsonSubTypes.Type(value = CompletionCheckEvent::class, name = COMPLETION_CHECK_KEY),
    JsonSubTypes.Type(value = ConnectivitySnapshotEvent::class, name = CONNECTIVITY_SNAPSHOT_KEY),
    JsonSubTypes.Type(value = ConsentEvent::class, name = CONSENT_KEY),
    JsonSubTypes.Type(value = EnrolmentEventV1::class, name = ENROLMENT_V1_KEY),
    JsonSubTypes.Type(value = EnrolmentEventV2::class, name = ENROLMENT_V2_KEY),
    JsonSubTypes.Type(value = FingerprintCaptureEventV3::class, name = FINGERPRINT_CAPTURE_V3_KEY),
    JsonSubTypes.Type(value = FingerprintCaptureBiometricsEvent::class, name = FINGERPRINT_CAPTURE_BIOMETRICS_KEY),
    JsonSubTypes.Type(value = GuidSelectionEvent::class, name = GUID_SELECTION_KEY),
    JsonSubTypes.Type(value = IntentParsingEvent::class, name = INTENT_PARSING_KEY),
    JsonSubTypes.Type(value = InvalidIntentEvent::class, name = INVALID_INTENT_KEY),
    JsonSubTypes.Type(value = OneToManyMatchEvent::class, name = ONE_TO_MANY_MATCH_KEY),
    JsonSubTypes.Type(value = OneToOneMatchEvent::class, name = ONE_TO_ONE_MATCH_KEY),
    JsonSubTypes.Type(value = PersonCreationEvent::class, name = PERSON_CREATION_KEY),
    JsonSubTypes.Type(value = RefusalEvent::class, name = REFUSAL_KEY),
    JsonSubTypes.Type(value = ScannerConnectionEvent::class, name = SCANNER_CONNECTION_KEY),
    JsonSubTypes.Type(
        value = ScannerFirmwareUpdateEvent::class,
        name = SCANNER_FIRMWARE_UPDATE_KEY
    ),
    JsonSubTypes.Type(value = SuspiciousIntentEvent::class, name = SUSPICIOUS_INTENT_KEY),
    JsonSubTypes.Type(value = Vero2InfoSnapshotEvent::class, name = VERO_2_INFO_SNAPSHOT_KEY)
)
abstract class Event {
    abstract val type: EventType
    abstract val id: String
    abstract var labels: EventLabels
    abstract val payload: EventPayload

    override fun equals(other: Any?): Boolean {
        return other is Event && other.id == id
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }

}

fun EventType.isNotASubjectEvent() =
    this != EventType.ENROLMENT_RECORD_CREATION &&
        this != EventType.ENROLMENT_RECORD_MOVE &&
        this != EventType.ENROLMENT_RECORD_DELETION
