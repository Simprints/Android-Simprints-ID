package com.simprints.id.data.db.event.remote

import androidx.annotation.Keep
import com.simprints.id.data.db.event.domain.events.*
import com.simprints.id.data.db.event.domain.events.EventType.*
import com.simprints.id.data.db.event.domain.events.callback.*
import com.simprints.id.data.db.event.domain.events.callout.*
import com.simprints.id.data.db.event.domain.events.session.SessionCaptureEvent
import com.simprints.id.data.db.event.domain.events.subject.EnrolmentRecordCreationEvent
import com.simprints.id.data.db.event.domain.events.subject.EnrolmentRecordDeletionEvent
import com.simprints.id.data.db.event.domain.events.subject.EnrolmentRecordMoveEvent
import com.simprints.id.data.db.event.remote.events.*
import com.simprints.id.data.db.event.remote.events.callback.ApiCallbackEvent
import com.simprints.id.data.db.event.remote.events.callout.ApiCalloutEvent
import com.simprints.id.data.db.event.remote.events.subject.ApiEnrolmentRecordCreationEvent
import com.simprints.id.data.db.event.remote.events.subject.ApiEnrolmentRecordDeletionEvent
import com.simprints.id.data.db.event.remote.events.subject.ApiEnrolmentRecordMoveEvent
import com.simprints.id.data.db.event.remote.session.ApiSessionCapture

@Keep
fun Event.toApiEvent(): ApiEvent =
    when (this.payload.type) {
        ARTIFICIAL_TERMINATION -> ApiArtificialTerminationEvent(this as ArtificialTerminationEvent)
        REFUSAL -> ApiRefusalEvent(this as RefusalEvent)
        CONSENT -> ApiConsentEvent(this as ConsentEvent)
        ENROLMENT -> ApiEnrolmentEvent(this as EnrolmentEvent)
        ALERT_SCREEN -> ApiAlertScreenEvent(this as AlertScreenEvent)
        CANDIDATE_READ -> ApiCandidateReadEvent(this as CandidateReadEvent)
        AUTHORIZATION -> ApiAuthorizationEvent(this as AuthorizationEvent)
        GUID_SELECTION -> ApiGuidSelectionEvent(this as GuidSelectionEvent)
        AUTHENTICATION -> ApiAuthenticationEvent(this as AuthenticationEvent)
        ONE_TO_ONE_MATCH -> ApiOneToOneMatchEvent(this as OneToOneMatchEvent)
        PERSON_CREATION -> ApiPersonCreationEvent(this as PersonCreationEvent)
        ONE_TO_MANY_MATCH -> ApiOneToManyMatchEvent(this as OneToManyMatchEvent)
        SCANNER_CONNECTION -> ApiScannerConnectionEvent(this as ScannerConnectionEvent)
        FINGERPRINT_CAPTURE -> ApiFingerprintCaptureEvent(this as FingerprintCaptureEvent)
        CONNECTIVITY_SNAPSHOT -> ApiConnectivitySnapshotEvent(this as ConnectivitySnapshotEvent)
        INVALID_INTENT -> ApiInvalidIntentEvent(this as InvalidIntentEvent)
        SUSPICIOUS_INTENT -> ApiSuspiciousIntentEvent(this as SuspiciousIntentEvent)
        COMPLETION_CHECK -> ApiCompletionCheckEvent(this as CompletionCheckEvent)
        INTENT_PARSING -> ApiIntentParsingEvent(this as IntentParsingEvent)
        CALLOUT_CONFIRMATION -> ApiCalloutEvent(this as ConfirmationCalloutEvent)
        CALLOUT_IDENTIFICATION -> ApiCalloutEvent(this as IdentificationCalloutEvent)
        CALLOUT_ENROLMENT -> ApiCalloutEvent(this as EnrolmentCalloutEvent)
        CALLOUT_VERIFICATION -> ApiCalloutEvent(this as VerificationCalloutEvent)
        CALLOUT_LAST_BIOMETRICS -> ApiCalloutEvent(this as EnrolmentLastBiometricsCalloutEvent)
        CALLBACK_IDENTIFICATION -> ApiCallbackEvent(this as IdentificationCallbackEvent)
        CALLBACK_ENROLMENT -> ApiCallbackEvent(this as EnrolmentCallbackEvent)
        CALLBACK_REFUSAL -> ApiCallbackEvent(this as RefusalCallbackEvent)
        CALLBACK_VERIFICATION -> ApiCallbackEvent(this as VerificationCallbackEvent)
        CALLBACK_CONFIRMATION -> ApiCallbackEvent(this as ConfirmationCallbackEvent)
        CALLBACK_ERROR -> ApiCallbackEvent(this as ErrorCallbackEvent)
        VERO_2_INFO_SNAPSHOT -> ApiVero2InfoSnapshotEvent(this as Vero2InfoSnapshotEvent)
        SCANNER_FIRMWARE_UPDATE -> ApiScannerFirmwareUpdateEvent(this as ScannerFirmwareUpdateEvent)
        ENROLMENT_RECORD_CREATION -> ApiEnrolmentRecordCreationEvent(this as EnrolmentRecordCreationEvent)
        ENROLMENT_RECORD_DELETION -> ApiEnrolmentRecordDeletionEvent(this as EnrolmentRecordDeletionEvent)
        ENROLMENT_RECORD_MOVE -> ApiEnrolmentRecordMoveEvent(this as EnrolmentRecordMoveEvent)
        SESSION_CAPTURE -> ApiSessionCapture(this as SessionCaptureEvent)
    }
