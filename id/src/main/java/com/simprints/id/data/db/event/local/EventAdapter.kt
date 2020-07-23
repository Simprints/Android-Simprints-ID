package com.simprints.id.data.db.event.local

import com.beust.klaxon.TypeAdapter
import com.simprints.id.data.db.event.domain.models.*
import com.simprints.id.data.db.event.domain.models.EventLabel.*
import com.simprints.id.data.db.event.domain.models.EventLabel.EventLabelKey.*
import com.simprints.id.data.db.event.domain.models.EventType.*
import com.simprints.id.data.db.event.domain.models.callback.*
import com.simprints.id.data.db.event.domain.models.callout.*
import com.simprints.id.data.db.event.domain.models.face.*
import com.simprints.id.data.db.event.domain.models.session.SessionCaptureEvent
import com.simprints.id.data.db.event.domain.models.subject.EnrolmentRecordCreationEvent
import com.simprints.id.data.db.event.domain.models.subject.EnrolmentRecordDeletionEvent
import com.simprints.id.data.db.event.domain.models.subject.EnrolmentRecordMoveEvent
import kotlin.reflect.KClass

class EventLabelAdapter : TypeAdapter<EventLabel> {
    override fun classFor(eventLabelKey: Any): KClass<out EventLabel> {
        val eventLabelName = eventLabelKey as String
        val eventLabel = EventLabelKey.valueOf(eventLabelName)
        return when (eventLabel) {
            PROJECT_ID -> ProjectIdLabel::class
            SUBJECT_ID -> SubjectIdLabel::class
            ATTENDANT_ID -> AttendantIdLabel::class
            MODULE_IDS -> ModuleIdsLabel::class
            MODES -> ModesLabel::class
            SESSION_ID -> SessionIdLabel::class
            DEVICE_ID -> DeviceIdLabel::class
        }
    }
}

class EventAdapter : TypeAdapter<Event> {
    override fun classFor(type: Any): KClass<out Event> {
        return when (EventType.valueOf(type as String)) {
            SESSION_CAPTURE -> SessionCaptureEvent::class
            ENROLMENT_RECORD_CREATION -> EnrolmentRecordCreationEvent::class
            ENROLMENT_RECORD_DELETION -> EnrolmentRecordDeletionEvent::class
            ENROLMENT_RECORD_MOVE -> EnrolmentRecordMoveEvent::class
            ARTIFICIAL_TERMINATION -> ArtificialTerminationEvent::class
            AUTHENTICATION -> AuthenticationEvent::class
            CONSENT -> ConsentEvent::class
            ENROLMENT -> EnrolmentEvent::class
            AUTHORIZATION -> AuthorizationEvent::class
            FINGERPRINT_CAPTURE -> FingerprintCaptureEvent::class
            ONE_TO_ONE_MATCH -> OneToOneMatchEvent::class
            ONE_TO_MANY_MATCH -> OneToManyMatchEvent::class
            PERSON_CREATION -> PersonCreationEvent::class
            ALERT_SCREEN -> AlertScreenEvent::class
            GUID_SELECTION -> GuidSelectionEvent::class
            CONNECTIVITY_SNAPSHOT -> ConnectivitySnapshotEvent::class
            REFUSAL -> RefusalEvent::class
            CANDIDATE_READ -> CandidateReadEvent::class
            SCANNER_CONNECTION -> ScannerConnectionEvent::class
            VERO_2_INFO_SNAPSHOT -> Vero2InfoSnapshotEvent::class
            SCANNER_FIRMWARE_UPDATE -> ScannerFirmwareUpdateEvent::class
            INVALID_INTENT -> InvalidIntentEvent::class
            CALLOUT_CONFIRMATION -> ConfirmationCalloutEvent::class
            CALLOUT_IDENTIFICATION -> IdentificationCalloutEvent::class
            CALLOUT_ENROLMENT -> EnrolmentCalloutEvent::class
            CALLOUT_VERIFICATION -> VerificationCalloutEvent::class
            CALLOUT_LAST_BIOMETRICS -> EnrolmentLastBiometricsCalloutEvent::class
            CALLBACK_IDENTIFICATION -> IdentificationCallbackEvent::class
            CALLBACK_ENROLMENT -> EnrolmentCallbackEvent::class
            CALLBACK_REFUSAL -> RefusalCallbackEvent::class
            CALLBACK_VERIFICATION -> VerificationCallbackEvent::class
            CALLBACK_ERROR -> ErrorCallbackEvent::class
            SUSPICIOUS_INTENT -> SuspiciousIntentEvent::class
            INTENT_PARSING -> IntentParsingEvent::class
            COMPLETION_CHECK -> CompletionCheckEvent::class
            CALLBACK_CONFIRMATION -> ConfirmationCallbackEvent::class
            FACE_ONBOARDING_COMPLETE -> FaceOnboardingCompleteEvent::class
            FACE_FALLBACK_CAPTURE -> FaceFallbackCaptureEvent::class
            FACE_CAPTURE -> FaceCaptureEvent::class
            FACE_CAPTURE_CONFIRMATION -> FaceCaptureConfirmationEvent::class
            FACE_CAPTURE_RETRY -> FaceCaptureRetryEvent::class
        }
    }
}
