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
import com.simprints.id.data.db.event.domain.events.EventPayloadType
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

@Keep
abstract class ApiEventPayload(
    val type: ApiEventPayloadType,
    val relativeStartTime: Int, //TODO: "relativeStartTime" to change
    val creationTime: Long
)

fun EventPayload.fromDomainToApi() =
    when (this.type) {
        EventPayloadType.ENROLMENT_RECORD_CREATION -> ApiEnrolmentRecordCreationPayload(this as EnrolmentRecordCreationPayload)
        EventPayloadType.ENROLMENT_RECORD_DELETION -> ApiEnrolmentRecordDeletionPayload(this as EnrolmentRecordDeletionPayload)
        EventPayloadType.ENROLMENT_RECORD_MOVE -> ApiEnrolmentRecordMovePayload(this as EnrolmentRecordMovePayload)
        EventPayloadType.ARTIFICIAL_TERMINATION -> ApiArtificialTerminationPayload(this as ArtificialTerminationPayload)
        EventPayloadType.AUTHENTICATION -> ApiAuthenticationPayload(this as AuthenticationPayload)
        EventPayloadType.CONSENT -> ApiConsentPayload(this as ConsentPayload)
        EventPayloadType.ENROLMENT -> ApiEnrolmentPayload(this as EnrolmentPayload)
        EventPayloadType.AUTHORIZATION -> ApiAuthorizationPayload(this as AuthorizationPayload)
        EventPayloadType.FINGERPRINT_CAPTURE -> ApiFingerprintCapturePayload(this as FingerprintCapturePayload)
        EventPayloadType.ONE_TO_ONE_MATCH -> ApiOneToOneMatchPayload(this as OneToOneMatchPayload)
        EventPayloadType.ONE_TO_MANY_MATCH -> ApiOneToManyMatchPayload(this as OneToManyMatchPayload)
        EventPayloadType.PERSON_CREATION -> ApiPersonCreationPayload(this as PersonCreationPayload)
        EventPayloadType.ALERT_SCREEN -> ApiAlertScreenPayload(this as AlertScreenPayload)
        EventPayloadType.GUID_SELECTION -> ApiGuidSelectionPayload(this as GuidSelectionPayload)
        EventPayloadType.CONNECTIVITY_SNAPSHOT -> ApiConnectivitySnapshotPayload(this as ConnectivitySnapshotPayload)
        EventPayloadType.REFUSAL -> ApiRefusalPayload(this as RefusalPayload)
        EventPayloadType.CANDIDATE_READ -> ApiCandidateReadPayload(this as CandidateReadPayload)
        EventPayloadType.SCANNER_CONNECTION -> ApiScannerConnectionPayload(this as ScannerConnectionPayload)
        EventPayloadType.VERO_2_INFO_SNAPSHOT -> ApiVero2InfoSnapshotPayload(this as Vero2InfoSnapshotPayload)
        EventPayloadType.SCANNER_FIRMWARE_UPDATE -> ApiScannerFirmwareUpdatePayload(this as ScannerFirmwareUpdatePayload)
        EventPayloadType.INVALID_INTENT -> ApiInvalidIntentPayload(this as InvalidIntentPayload)
        EventPayloadType.CALLOUT_CONFIRMATION -> ApiCalloutPayload(this as ConfirmationCalloutPayload)
        EventPayloadType.CALLOUT_IDENTIFICATION -> ApiCalloutPayload(this as IdentificationCalloutPayload)
        EventPayloadType.CALLOUT_ENROLMENT -> ApiCalloutPayload(this as EnrolmentCalloutPayload)
        EventPayloadType.CALLOUT_VERIFICATION -> ApiCalloutPayload(this as VerificationCalloutPayload)
        EventPayloadType.CALLOUT_LAST_BIOMETRICS -> ApiCalloutPayload(this as EnrolmentLastBiometricsCalloutPayload)
        EventPayloadType.CALLBACK_IDENTIFICATION -> ApiCallbackPayload(this as IdentificationCallbackPayload)
        EventPayloadType.CALLBACK_ENROLMENT -> ApiCallbackPayload(this as EnrolmentCallbackPayload)
        EventPayloadType.CALLBACK_REFUSAL -> ApiCallbackPayload(this as RefusalCallbackPayload)
        EventPayloadType.CALLBACK_VERIFICATION -> ApiCallbackPayload(this as VerificationCallbackPayload)
        EventPayloadType.CALLBACK_ERROR -> ApiCallbackPayload(this as ErrorCallbackPayload)
        EventPayloadType.CALLBACK_CONFIRMATION -> ApiCallbackPayload(this as ConfirmationCallbackPayload)
        EventPayloadType.SUSPICIOUS_INTENT -> ApiSuspiciousIntentPayload(this as SuspiciousIntentPayload)
        EventPayloadType.INTENT_PARSING -> ApiIntentParsingPayload(this as IntentParsingPayload)
        EventPayloadType.COMPLETION_CHECK -> ApiCompletionCheckPayload(this as CompletionCheckPayload)
    }
