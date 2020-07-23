package com.simprints.id.data.db.event.remote.events

import androidx.annotation.Keep
import com.simprints.id.data.db.event.domain.models.AlertScreenEvent.AlertScreenPayload
import com.simprints.id.data.db.event.domain.models.ArtificialTerminationEvent.ArtificialTerminationPayload
import com.simprints.id.data.db.event.domain.models.AuthenticationEvent.AuthenticationPayload
import com.simprints.id.data.db.event.domain.models.AuthorizationEvent.AuthorizationPayload
import com.simprints.id.data.db.event.domain.models.CandidateReadEvent.CandidateReadPayload
import com.simprints.id.data.db.event.domain.models.CompletionCheckEvent.CompletionCheckPayload
import com.simprints.id.data.db.event.domain.models.ConnectivitySnapshotEvent.ConnectivitySnapshotPayload
import com.simprints.id.data.db.event.domain.models.ConsentEvent.ConsentPayload
import com.simprints.id.data.db.event.domain.models.EnrolmentEvent.EnrolmentPayload
import com.simprints.id.data.db.event.domain.models.EventPayload
import com.simprints.id.data.db.event.domain.models.EventType.*
import com.simprints.id.data.db.event.domain.models.FingerprintCaptureEvent.FingerprintCapturePayload
import com.simprints.id.data.db.event.domain.models.GuidSelectionEvent.GuidSelectionPayload
import com.simprints.id.data.db.event.domain.models.IntentParsingEvent.IntentParsingPayload
import com.simprints.id.data.db.event.domain.models.InvalidIntentEvent.InvalidIntentPayload
import com.simprints.id.data.db.event.domain.models.OneToManyMatchEvent.OneToManyMatchPayload
import com.simprints.id.data.db.event.domain.models.OneToOneMatchEvent.OneToOneMatchPayload
import com.simprints.id.data.db.event.domain.models.PersonCreationEvent.PersonCreationPayload
import com.simprints.id.data.db.event.domain.models.RefusalEvent.RefusalPayload
import com.simprints.id.data.db.event.domain.models.ScannerConnectionEvent.ScannerConnectionPayload
import com.simprints.id.data.db.event.domain.models.ScannerFirmwareUpdateEvent.ScannerFirmwareUpdatePayload
import com.simprints.id.data.db.event.domain.models.SuspiciousIntentEvent.SuspiciousIntentPayload
import com.simprints.id.data.db.event.domain.models.Vero2InfoSnapshotEvent.Vero2InfoSnapshotPayload
import com.simprints.id.data.db.event.domain.models.callback.ConfirmationCallbackEvent.ConfirmationCallbackPayload
import com.simprints.id.data.db.event.domain.models.callback.EnrolmentCallbackEvent.EnrolmentCallbackPayload
import com.simprints.id.data.db.event.domain.models.callback.ErrorCallbackEvent.ErrorCallbackPayload
import com.simprints.id.data.db.event.domain.models.callback.IdentificationCallbackEvent.IdentificationCallbackPayload
import com.simprints.id.data.db.event.domain.models.callback.RefusalCallbackEvent.RefusalCallbackPayload
import com.simprints.id.data.db.event.domain.models.callback.VerificationCallbackEvent.VerificationCallbackPayload
import com.simprints.id.data.db.event.domain.models.callout.ConfirmationCalloutEvent.ConfirmationCalloutPayload
import com.simprints.id.data.db.event.domain.models.callout.EnrolmentCalloutEvent.EnrolmentCalloutPayload
import com.simprints.id.data.db.event.domain.models.callout.EnrolmentLastBiometricsCalloutEvent.EnrolmentLastBiometricsCalloutPayload
import com.simprints.id.data.db.event.domain.models.callout.IdentificationCalloutEvent.IdentificationCalloutPayload
import com.simprints.id.data.db.event.domain.models.callout.VerificationCalloutEvent.VerificationCalloutPayload
import com.simprints.id.data.db.event.domain.models.face.FaceCaptureConfirmationEvent.FaceCaptureConfirmationPayload
import com.simprints.id.data.db.event.domain.models.face.FaceCaptureEvent.FaceCapturePayload
import com.simprints.id.data.db.event.domain.models.face.FaceCaptureRetryEvent.FaceCaptureRetryPayload
import com.simprints.id.data.db.event.domain.models.face.FaceFallbackCaptureEvent.FaceFallbackCapturePayload
import com.simprints.id.data.db.event.domain.models.face.FaceOnboardingCompleteEvent.FaceOnboardingCompletePayload
import com.simprints.id.data.db.event.domain.models.session.SessionCaptureEvent.SessionCapturePayload
import com.simprints.id.data.db.event.domain.models.subject.EnrolmentRecordCreationEvent.EnrolmentRecordCreationPayload
import com.simprints.id.data.db.event.domain.models.subject.EnrolmentRecordDeletionEvent.EnrolmentRecordDeletionPayload
import com.simprints.id.data.db.event.domain.models.subject.EnrolmentRecordMoveEvent.EnrolmentRecordMovePayload
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
import com.simprints.id.data.db.event.remote.events.face.ApiFaceCaptureConfirmationEvent.ApiFaceCaptureConfirmationPayload
import com.simprints.id.data.db.event.remote.events.face.ApiFaceCaptureEvent.ApiFaceCapturePayload
import com.simprints.id.data.db.event.remote.events.face.ApiFaceCaptureRetryEvent.ApiFaceCaptureRetryPayload
import com.simprints.id.data.db.event.remote.events.face.ApiFaceFallbackCaptureEvent.ApiFaceFallbackCapturePayload
import com.simprints.id.data.db.event.remote.events.face.ApiFaceOnboardingCompleteEvent.ApiFaceOnboardingCompletePayload
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

fun EventPayload.fromDomainToApi(): ApiEventPayload =
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
        FACE_ONBOARDING_COMPLETE -> ApiFaceOnboardingCompletePayload(this as FaceOnboardingCompletePayload)
        FACE_FALLBACK_CAPTURE -> ApiFaceFallbackCapturePayload(this as FaceFallbackCapturePayload)
        FACE_CAPTURE -> ApiFaceCapturePayload(this as FaceCapturePayload)
        FACE_CAPTURE_CONFIRMATION -> ApiFaceCaptureConfirmationPayload(this as FaceCaptureConfirmationPayload)
        FACE_CAPTURE_RETRY -> ApiFaceCaptureRetryPayload(this as FaceCaptureRetryPayload)
    }
