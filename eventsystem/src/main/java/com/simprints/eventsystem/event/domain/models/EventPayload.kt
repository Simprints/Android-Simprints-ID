package com.simprints.eventsystem.event.domain.models

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.simprints.eventsystem.event.domain.models.AlertScreenEvent.AlertScreenPayload
import com.simprints.eventsystem.event.domain.models.ArtificialTerminationEvent.ArtificialTerminationPayload
import com.simprints.eventsystem.event.domain.models.AuthenticationEvent.AuthenticationPayload
import com.simprints.eventsystem.event.domain.models.AuthorizationEvent.AuthorizationPayload
import com.simprints.eventsystem.event.domain.models.CandidateReadEvent.CandidateReadPayload
import com.simprints.eventsystem.event.domain.models.CompletionCheckEvent.CompletionCheckPayload
import com.simprints.eventsystem.event.domain.models.ConnectivitySnapshotEvent.ConnectivitySnapshotPayload
import com.simprints.eventsystem.event.domain.models.ConsentEvent.ConsentPayload
import com.simprints.eventsystem.event.domain.models.EventType.Companion
import com.simprints.eventsystem.event.domain.models.GuidSelectionEvent.GuidSelectionPayload
import com.simprints.eventsystem.event.domain.models.IntentParsingEvent.IntentParsingPayload
import com.simprints.eventsystem.event.domain.models.InvalidIntentEvent.InvalidIntentPayload
import com.simprints.eventsystem.event.domain.models.OneToManyMatchEvent.OneToManyMatchPayload
import com.simprints.eventsystem.event.domain.models.OneToOneMatchEvent.OneToOneMatchPayload
import com.simprints.eventsystem.event.domain.models.PersonCreationEvent.PersonCreationPayload
import com.simprints.eventsystem.event.domain.models.RefusalEvent.RefusalPayload
import com.simprints.eventsystem.event.domain.models.ScannerConnectionEvent.ScannerConnectionPayload
import com.simprints.eventsystem.event.domain.models.ScannerFirmwareUpdateEvent.ScannerFirmwareUpdatePayload
import com.simprints.eventsystem.event.domain.models.SuspiciousIntentEvent.SuspiciousIntentPayload
import com.simprints.eventsystem.event.domain.models.Vero2InfoSnapshotEvent.Vero2InfoSnapshotPayload
import com.simprints.eventsystem.event.domain.models.callback.ConfirmationCallbackEvent.ConfirmationCallbackPayload
import com.simprints.eventsystem.event.domain.models.callback.EnrolmentCallbackEvent.EnrolmentCallbackPayload
import com.simprints.eventsystem.event.domain.models.callback.ErrorCallbackEvent.ErrorCallbackPayload
import com.simprints.eventsystem.event.domain.models.callback.IdentificationCallbackEvent.IdentificationCallbackPayload
import com.simprints.eventsystem.event.domain.models.callback.RefusalCallbackEvent.RefusalCallbackPayload
import com.simprints.eventsystem.event.domain.models.callback.VerificationCallbackEvent.VerificationCallbackPayload
import com.simprints.eventsystem.event.domain.models.callout.ConfirmationCalloutEvent.ConfirmationCalloutPayload
import com.simprints.eventsystem.event.domain.models.callout.EnrolmentCalloutEvent.EnrolmentCalloutPayload
import com.simprints.eventsystem.event.domain.models.callout.EnrolmentLastBiometricsCalloutEvent.EnrolmentLastBiometricsCalloutPayload
import com.simprints.eventsystem.event.domain.models.callout.IdentificationCalloutEvent.IdentificationCalloutPayload
import com.simprints.eventsystem.event.domain.models.callout.VerificationCalloutEvent.VerificationCalloutPayload
import com.simprints.eventsystem.event.domain.models.face.FaceCaptureBiometricsEvent
import com.simprints.eventsystem.event.domain.models.face.FaceCaptureConfirmationEvent.FaceCaptureConfirmationPayload
import com.simprints.eventsystem.event.domain.models.face.FaceCaptureEvent
import com.simprints.eventsystem.event.domain.models.face.FaceFallbackCaptureEvent.FaceFallbackCapturePayload
import com.simprints.eventsystem.event.domain.models.face.FaceOnboardingCompleteEvent.FaceOnboardingCompletePayload
import com.simprints.eventsystem.event.domain.models.fingerprint.FingerprintCaptureBiometricsEvent
import com.simprints.eventsystem.event.domain.models.fingerprint.FingerprintCaptureEvent
import com.simprints.eventsystem.event.domain.models.session.SessionCaptureEvent.SessionCapturePayload

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "type", visible = true)
@JsonSubTypes(
    JsonSubTypes.Type(value = ConfirmationCallbackPayload::class, name = EventType.CALLBACK_CONFIRMATION_KEY),
    JsonSubTypes.Type(value = EnrolmentCallbackPayload::class, name = EventType.CALLBACK_ENROLMENT_KEY),
    JsonSubTypes.Type(value = ErrorCallbackPayload::class, name = EventType.CALLBACK_ERROR_KEY),
    JsonSubTypes.Type(value = IdentificationCallbackPayload::class, name = EventType.CALLBACK_IDENTIFICATION_KEY),
    JsonSubTypes.Type(value = RefusalCallbackPayload::class, name = EventType.CALLBACK_REFUSAL_KEY),
    JsonSubTypes.Type(value = VerificationCallbackPayload::class, name = EventType.CALLBACK_VERIFICATION_KEY),
    JsonSubTypes.Type(value = ConfirmationCalloutPayload::class, name = EventType.CALLOUT_CONFIRMATION_KEY),
    JsonSubTypes.Type(value = EnrolmentCalloutPayload::class, name = EventType.CALLOUT_ENROLMENT_KEY),
    JsonSubTypes.Type(value = EnrolmentLastBiometricsCalloutPayload::class, name = EventType.CALLOUT_LAST_BIOMETRICS_KEY),
    JsonSubTypes.Type(value = IdentificationCalloutPayload::class, name = EventType.CALLOUT_IDENTIFICATION_KEY),
    JsonSubTypes.Type(value = VerificationCalloutPayload::class, name = EventType.CALLOUT_VERIFICATION_KEY),
    JsonSubTypes.Type(value = FaceCaptureConfirmationPayload::class, name = EventType.FACE_CAPTURE_CONFIRMATION_KEY),
    JsonSubTypes.Type(value = FaceCaptureEvent.FaceCapturePayload::class, name = EventType.FACE_CAPTURE_KEY),
    JsonSubTypes.Type(value = FaceCaptureBiometricsEvent.FaceCaptureBiometricsPayload::class, name = EventType.FACE_CAPTURE_BIOMETRICS_KEY),
    JsonSubTypes.Type(value = FaceFallbackCapturePayload::class, name = EventType.FACE_FALLBACK_CAPTURE_KEY),
    JsonSubTypes.Type(value = FaceOnboardingCompletePayload::class, name = EventType.FACE_ONBOARDING_COMPLETE_KEY),
    JsonSubTypes.Type(value = SessionCapturePayload::class, name = EventType.SESSION_CAPTURE_KEY),
    JsonSubTypes.Type(value = AlertScreenPayload::class, name = EventType.ALERT_SCREEN_KEY),
    JsonSubTypes.Type(value = ArtificialTerminationPayload::class, name = EventType.ARTIFICIAL_TERMINATION_KEY),
    JsonSubTypes.Type(value = AuthenticationPayload::class, name = EventType.AUTHENTICATION_KEY),
    JsonSubTypes.Type(value = AuthorizationPayload::class, name = EventType.AUTHORIZATION_KEY),
    JsonSubTypes.Type(value = CandidateReadPayload::class, name = EventType.CANDIDATE_READ_KEY),
    JsonSubTypes.Type(value = CompletionCheckPayload::class, name = EventType.COMPLETION_CHECK_KEY),
    JsonSubTypes.Type(value = ConnectivitySnapshotPayload::class, name = EventType.CONNECTIVITY_SNAPSHOT_KEY),
    JsonSubTypes.Type(value = ConsentPayload::class, name = EventType.CONSENT_KEY),
    JsonSubTypes.Type(value = EnrolmentEventV1.EnrolmentPayload::class, name = EventType.ENROLMENT_V1_KEY),
    JsonSubTypes.Type(value = EnrolmentEventV2.EnrolmentPayload::class, name = EventType.ENROLMENT_V2_KEY),
    JsonSubTypes.Type(value = FingerprintCaptureEvent.FingerprintCapturePayload::class, name = EventType.FINGERPRINT_CAPTURE_KEY),
    JsonSubTypes.Type(value = FingerprintCaptureBiometricsEvent.FingerprintCaptureBiometricsPayload::class, name = EventType.FINGERPRINT_CAPTURE_BIOMETRICS_KEY),
    JsonSubTypes.Type(value = GuidSelectionPayload::class, name = EventType.GUID_SELECTION_KEY),
    JsonSubTypes.Type(value = IntentParsingPayload::class, name = EventType.INTENT_PARSING_KEY),
    JsonSubTypes.Type(value = InvalidIntentPayload::class, name = EventType.INVALID_INTENT_KEY),
    JsonSubTypes.Type(value = OneToManyMatchPayload::class, name = EventType.ONE_TO_MANY_MATCH_KEY),
    JsonSubTypes.Type(value = OneToOneMatchPayload::class, name = EventType.ONE_TO_ONE_MATCH_KEY),
    JsonSubTypes.Type(value = PersonCreationPayload::class, name = EventType.PERSON_CREATION_KEY),
    JsonSubTypes.Type(value = RefusalPayload::class, name = EventType.REFUSAL_KEY),
    JsonSubTypes.Type(value = ScannerConnectionPayload::class, name = EventType.SCANNER_CONNECTION_KEY),
    JsonSubTypes.Type(value = ScannerFirmwareUpdatePayload::class, name = Companion.SCANNER_FIRMWARE_UPDATE_KEY),
    JsonSubTypes.Type(value = Vero2InfoSnapshotPayload::class, name = EventType.VERO_2_INFO_SNAPSHOT_KEY),
    JsonSubTypes.Type(value = SuspiciousIntentPayload::class, name = EventType.SUSPICIOUS_INTENT_KEY)
)
abstract class EventPayload {
    abstract val type: EventType
    abstract val eventVersion: Int
    abstract val createdAt: Long
    abstract val endedAt: Long
}
