package com.simprints.id.data.db.event.remote.events

import androidx.annotation.Keep
import com.simprints.id.data.db.event.domain.events.AlertScreenEvent.AlertScreenPayload
import com.simprints.id.data.db.event.domain.events.ArtificialTerminationEvent.ArtificialTerminationPayload
import com.simprints.id.data.db.event.domain.events.AuthenticationEvent.AuthenticationPayload
import com.simprints.id.data.db.event.domain.events.AuthorizationEvent.AuthorizationPayload
import com.simprints.id.data.db.event.domain.events.CandidateReadEvent.CandidateReadPayload
import com.simprints.id.data.db.event.domain.events.CompletionCheckEvent.CompletionCheckPayload
import com.simprints.id.data.db.event.domain.events.ConnectivitySnapshotEvent.ConnectivitySnapshotPayload
import com.simprints.id.data.db.event.domain.events.ConsentEvent.ConsentPayload
import com.simprints.id.data.db.event.domain.events.EnrolmentEvent.EnrolmentPayload
import com.simprints.id.data.db.event.domain.events.EventPayload
import com.simprints.id.data.db.event.domain.events.EventType.*
import com.simprints.id.data.db.event.domain.events.FingerprintCaptureEvent.FingerprintCapturePayload
import com.simprints.id.data.db.event.domain.events.GuidSelectionEvent.GuidSelectionPayload
import com.simprints.id.data.db.event.domain.events.IntentParsingEvent.IntentParsingPayload
import com.simprints.id.data.db.event.domain.events.InvalidIntentEvent.InvalidIntentPayload
import com.simprints.id.data.db.event.domain.events.OneToManyMatchEvent.OneToManyMatchPayload
import com.simprints.id.data.db.event.domain.events.OneToOneMatchEvent.OneToOneMatchPayload
import com.simprints.id.data.db.event.domain.events.PersonCreationEvent.PersonCreationPayload
import com.simprints.id.data.db.event.domain.events.RefusalEvent.RefusalPayload
import com.simprints.id.data.db.event.domain.events.ScannerConnectionEvent.ScannerConnectionPayload
import com.simprints.id.data.db.event.domain.events.ScannerFirmwareUpdateEvent.ScannerFirmwareUpdatePayload
import com.simprints.id.data.db.event.domain.events.SuspiciousIntentEvent.SuspiciousIntentPayload
import com.simprints.id.data.db.event.domain.events.Vero2InfoSnapshotEvent.Vero2InfoSnapshotPayload
import com.simprints.id.data.db.event.domain.events.callback.ConfirmationCallbackEvent.ConfirmationCallbackPayload
import com.simprints.id.data.db.event.domain.events.callback.EnrolmentCallbackEvent.EnrolmentCallbackPayload
import com.simprints.id.data.db.event.domain.events.callback.ErrorCallbackEvent.ErrorCallbackPayload
import com.simprints.id.data.db.event.domain.events.callback.IdentificationCallbackEvent.IdentificationCallbackPayload
import com.simprints.id.data.db.event.domain.events.callback.RefusalCallbackEvent.RefusalCallbackPayload
import com.simprints.id.data.db.event.domain.events.callback.VerificationCallbackEvent.VerificationCallbackPayload
import com.simprints.id.data.db.event.domain.events.callout.ConfirmationCalloutEvent.ConfirmationCalloutPayload
import com.simprints.id.data.db.event.domain.events.callout.EnrolmentCalloutEvent.EnrolmentCalloutPayload
import com.simprints.id.data.db.event.domain.events.callout.EnrolmentLastBiometricsCalloutEvent.EnrolmentLastBiometricsCalloutPayload
import com.simprints.id.data.db.event.domain.events.callout.IdentificationCalloutEvent.IdentificationCalloutPayload
import com.simprints.id.data.db.event.domain.events.callout.VerificationCalloutEvent.VerificationCalloutPayload
import com.simprints.id.data.db.event.domain.events.session.SessionCaptureEvent.SessionCapturePayload
import com.simprints.id.data.db.event.domain.events.subject.EnrolmentRecordCreationEvent.EnrolmentRecordCreationPayload
import com.simprints.id.data.db.event.domain.events.subject.EnrolmentRecordDeletionEvent.EnrolmentRecordDeletionPayload
import com.simprints.id.data.db.event.domain.events.subject.EnrolmentRecordMoveEvent.EnrolmentRecordMovePayload
import com.simprints.id.data.db.event.remote.events.ApiAlertScreenEvent.ApiAlertScreenPayload
import com.simprints.id.data.db.event.remote.events.ApiArtificialTerminationEvent.ApiArtificialTerminationPayload
import com.simprints.id.data.db.event.remote.events.ApiAuthenticationEvent.ApiAuthenticationPayload
import com.simprints.id.data.db.event.remote.events.ApiAuthorizationEvent.ApiAuthorizationPayload
import com.simprints.id.data.db.event.remote.events.ApiCandidateReadEvent.ApiCandidateReadPayload
import com.simprints.id.data.db.event.remote.events.ApiCompletionCheckEvent.ApiCompletionCheckPayload
import com.simprints.id.data.db.event.remote.events.ApiConnectivitySnapshotEvent.ApiConnectivitySnapshotPayload
import com.simprints.id.data.db.event.remote.events.ApiConsentEvent.ApiConsentPayload
import com.simprints.id.data.db.event.remote.events.ApiEnrolmentEvent.ApiEnrolmentPayload
import com.simprints.id.data.db.event.remote.events.ApiFingerprintCaptureEvent.ApiFingerprintCapturePayload
import com.simprints.id.data.db.event.remote.events.ApiGuidSelectionEvent.ApiGuidSelectionPayload
import com.simprints.id.data.db.event.remote.events.ApiIntentParsingEvent.ApiIntentParsingPayload
import com.simprints.id.data.db.event.remote.events.ApiInvalidIntentEvent.ApiInvalidIntentPayload
import com.simprints.id.data.db.event.remote.events.ApiOneToManyMatchEvent.ApiOneToManyMatchPayload
import com.simprints.id.data.db.event.remote.events.ApiOneToOneMatchEvent.ApiOneToOneMatchPayload
import com.simprints.id.data.db.event.remote.events.ApiPersonCreationEvent.ApiPersonCreationPayload
import com.simprints.id.data.db.event.remote.events.ApiRefusalEvent.ApiRefusalPayload
import com.simprints.id.data.db.event.remote.events.ApiScannerConnectionEvent.ApiScannerConnectionPayload
import com.simprints.id.data.db.event.remote.events.ApiScannerFirmwareUpdateEvent.ApiScannerFirmwareUpdatePayload
import com.simprints.id.data.db.event.remote.events.ApiSuspiciousIntentEvent.ApiSuspiciousIntentPayload
import com.simprints.id.data.db.event.remote.events.ApiVero2InfoSnapshotEvent.ApiVero2InfoSnapshotPayload
import com.simprints.id.data.db.event.remote.events.callback.ApiCallbackEvent.ApiCallbackPayload
import com.simprints.id.data.db.event.remote.events.callout.ApiCalloutEvent.ApiCalloutPayload
import com.simprints.id.data.db.event.remote.events.subject.ApiEnrolmentRecordCreationEvent.ApiEnrolmentRecordCreationPayload
import com.simprints.id.data.db.event.remote.events.subject.ApiEnrolmentRecordDeletionEvent.ApiEnrolmentRecordDeletionPayload
import com.simprints.id.data.db.event.remote.events.subject.ApiEnrolmentRecordMoveEvent.ApiEnrolmentRecordMovePayload
import com.simprints.id.data.db.event.remote.session.ApiSessionCapture.ApiSessionCapturePayload

@Keep
abstract class ApiEventPayload(
    val type: ApiEventPayloadType,
    val version: Int, //TODO: "relativeStartTime" to change
    val createdAt: Long
)

fun EventPayload.fromDomainToApi() =
    when (this.type) {
        ENROLMENT_RECORD_CREATION -> ApiEnrolmentRecordCreationPayload(this as EnrolmentRecordCreationPayload)
        ENROLMENT_RECORD_DELETION -> ApiEnrolmentRecordDeletionPayload(this as EnrolmentRecordDeletionPayload)
        ENROLMENT_RECORD_MOVE -> ApiEnrolmentRecordMovePayload(this as EnrolmentRecordMovePayload)
        ARTIFICIAL_TERMINATION -> ApiArtificialTerminationPayload(this as ArtificialTerminationPayload)
        AUTHENTICATION -> ApiAuthenticationPayload(this as AuthenticationPayload)
        CONSENT -> ApiConsentPayload(this as ConsentPayload)
        ENROLMENT -> ApiEnrolmentPayload(this as EnrolmentPayload)
        AUTHORIZATION -> ApiAuthorizationPayload(this as AuthorizationPayload)
        FINGERPRINT_CAPTURE -> ApiFingerprintCapturePayload(this as FingerprintCapturePayload)
        ONE_TO_ONE_MATCH -> ApiOneToOneMatchPayload(this as OneToOneMatchPayload)
        ONE_TO_MANY_MATCH -> ApiOneToManyMatchPayload(this as OneToManyMatchPayload)
        PERSON_CREATION -> ApiPersonCreationPayload(this as PersonCreationPayload)
        ALERT_SCREEN -> ApiAlertScreenPayload(this as AlertScreenPayload)
        GUID_SELECTION -> ApiGuidSelectionPayload(this as GuidSelectionPayload)
        CONNECTIVITY_SNAPSHOT -> ApiConnectivitySnapshotPayload(this as ConnectivitySnapshotPayload)
        REFUSAL -> ApiRefusalPayload(this as RefusalPayload)
        CANDIDATE_READ -> ApiCandidateReadPayload(this as CandidateReadPayload)
        SCANNER_CONNECTION -> ApiScannerConnectionPayload(this as ScannerConnectionPayload)
        VERO_2_INFO_SNAPSHOT -> ApiVero2InfoSnapshotPayload(this as Vero2InfoSnapshotPayload)
        SCANNER_FIRMWARE_UPDATE -> ApiScannerFirmwareUpdatePayload(this as ScannerFirmwareUpdatePayload)
        INVALID_INTENT -> ApiInvalidIntentPayload(this as InvalidIntentPayload)
        CALLOUT_CONFIRMATION -> ApiCalloutPayload(this as ConfirmationCalloutPayload)
        CALLOUT_IDENTIFICATION -> ApiCalloutPayload(this as IdentificationCalloutPayload)
        CALLOUT_ENROLMENT -> ApiCalloutPayload(this as EnrolmentCalloutPayload)
        CALLOUT_VERIFICATION -> ApiCalloutPayload(this as VerificationCalloutPayload)
        CALLOUT_LAST_BIOMETRICS -> ApiCalloutPayload(this as EnrolmentLastBiometricsCalloutPayload)
        CALLBACK_IDENTIFICATION -> ApiCallbackPayload(this as IdentificationCallbackPayload)
        CALLBACK_ENROLMENT -> ApiCallbackPayload(this as EnrolmentCallbackPayload)
        CALLBACK_REFUSAL -> ApiCallbackPayload(this as RefusalCallbackPayload)
        CALLBACK_VERIFICATION -> ApiCallbackPayload(this as VerificationCallbackPayload)
        CALLBACK_ERROR -> ApiCallbackPayload(this as ErrorCallbackPayload)
        CALLBACK_CONFIRMATION -> ApiCallbackPayload(this as ConfirmationCallbackPayload)
        SUSPICIOUS_INTENT -> ApiSuspiciousIntentPayload(this as SuspiciousIntentPayload)
        INTENT_PARSING -> ApiIntentParsingPayload(this as IntentParsingPayload)
        COMPLETION_CHECK -> ApiCompletionCheckPayload(this as CompletionCheckPayload)
        SESSION_CAPTURE -> ApiSessionCapturePayload(this as SessionCapturePayload)
    }
