package com.simprints.id.data.db.event.remote.models

import androidx.annotation.Keep
import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
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
import com.simprints.id.data.db.event.remote.models.ApiEventPayloadType.Companion
import com.simprints.id.data.db.event.remote.models.callback.ApiCallbackPayload
import com.simprints.id.data.db.event.remote.models.callout.ApiCalloutPayload
import com.simprints.id.data.db.event.remote.models.face.*
import com.simprints.id.data.db.event.remote.models.session.ApiSessionCapturePayload
import com.simprints.id.data.db.event.remote.models.subject.ApiEnrolmentRecordCreationPayload
import com.simprints.id.data.db.event.remote.models.subject.ApiEnrolmentRecordDeletionPayload
import com.simprints.id.data.db.event.remote.models.subject.ApiEnrolmentRecordMovePayload

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "type", visible = true)
@JsonSubTypes(
    JsonSubTypes.Type(value = ApiFaceCaptureConfirmationPayload::class, name = Companion.FACE_CAPTURE_CONFIRMATION_KEY),
    JsonSubTypes.Type(value = ApiFaceCapturePayload::class, name = Companion.FACE_CAPTURE_KEY),
    JsonSubTypes.Type(value = ApiFaceCaptureRetryPayload::class, name = Companion.FACE_CAPTURE_RETRY_KEY),
    JsonSubTypes.Type(value = ApiFaceFallbackCapturePayload::class, name = Companion.FACE_FALLBACK_CAPTURE_KEY),
    JsonSubTypes.Type(value = ApiFaceOnboardingCompletePayload::class, name = Companion.FACE_ONBOARDING_COMPLETE_KEY),
    JsonSubTypes.Type(value = ApiSessionCapturePayload::class, name = Companion.SESSION_CAPTURE_KEY),
    JsonSubTypes.Type(value = ApiEnrolmentRecordCreationPayload::class, name = Companion.ENROLMENT_RECORD_CREATION_KEY),
    JsonSubTypes.Type(value = ApiEnrolmentRecordDeletionPayload::class, name = Companion.ENROLMENT_RECORD_DELETION_KEY),
    JsonSubTypes.Type(value = ApiEnrolmentRecordMovePayload::class, name = Companion.ENROLMENT_RECORD_MOVE_KEY),
    JsonSubTypes.Type(value = ApiAlertScreenPayload::class, name = Companion.ALERT_SCREEN_KEY),
    JsonSubTypes.Type(value = ApiArtificialTerminationPayload::class, name = Companion.ARTIFICIAL_TERMINATION_KEY),
    JsonSubTypes.Type(value = ApiAuthenticationPayload::class, name = Companion.AUTHENTICATION_KEY),
    JsonSubTypes.Type(value = ApiAuthorizationPayload::class, name = Companion.AUTHORIZATION_KEY),
    JsonSubTypes.Type(value = ApiCandidateReadPayload::class, name = Companion.CANDIDATE_READ_KEY),
    JsonSubTypes.Type(value = ApiCompletionCheckPayload::class, name = Companion.COMPLETION_CHECK_KEY),
    JsonSubTypes.Type(value = ApiConnectivitySnapshotPayload::class, name = Companion.CONNECTIVITY_SNAPSHOT_KEY),
    JsonSubTypes.Type(value = ApiConsentPayload::class, name = Companion.CONSENT_KEY),
    JsonSubTypes.Type(value = ApiEnrolmentPayload::class, name = Companion.ENROLMENT_KEY),
    JsonSubTypes.Type(value = ApiFingerprintCapturePayload::class, name = Companion.FINGERPRINT_CAPTURE_KEY),
    JsonSubTypes.Type(value = ApiGuidSelectionPayload::class, name = Companion.GUID_SELECTION_KEY),
    JsonSubTypes.Type(value = ApiIntentParsingPayload::class, name = Companion.INTENT_PARSING_KEY),
    JsonSubTypes.Type(value = ApiInvalidIntentPayload::class, name = Companion.INVALID_INTENT_KEY),
    JsonSubTypes.Type(value = ApiOneToManyMatchPayload::class, name = Companion.ONE_TO_MANY_MATCH_KEY),
    JsonSubTypes.Type(value = ApiOneToOneMatchPayload::class, name = Companion.ONE_TO_ONE_MATCH_KEY),
    JsonSubTypes.Type(value = ApiPersonCreationPayload::class, name = Companion.PERSON_CREATION_KEY),
    JsonSubTypes.Type(value = ApiRefusalPayload::class, name = Companion.REFUSAL_KEY),
    JsonSubTypes.Type(value = ApiScannerConnectionPayload::class, name = Companion.SCANNER_CONNECTION_KEY),
    JsonSubTypes.Type(value = ApiScannerFirmwareUpdatePayload::class, name = Companion.SCANNER_FIRMWARE_UPDATE_KEY),
    JsonSubTypes.Type(value = ApiSuspiciousIntentPayload::class, name = Companion.SUSPICIOUS_INTENT_KEY),
    JsonSubTypes.Type(value = ApiVero2InfoSnapshotPayload::class, name = Companion.VERO_2_INFO_SNAPSHOT_KEY),
    JsonSubTypes.Type(value = ApiCallbackPayload::class, name = Companion.CALLOUT_KEY),
    JsonSubTypes.Type(value = ApiCalloutPayload::class, name = Companion.CALLBACK_KEY)
)
@Keep
abstract class ApiEventPayload(
    val type: ApiEventPayloadType,
    open val version: Int,
    open val startTime: Long
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
        INVALID_INTENT -> ApiInvalidIntentPayload(this as InvalidIntentPayload)
        SUSPICIOUS_INTENT -> ApiSuspiciousIntentPayload(this as SuspiciousIntentPayload)
        INTENT_PARSING -> ApiIntentParsingPayload(this as IntentParsingPayload)
        COMPLETION_CHECK -> ApiCompletionCheckPayload(this as CompletionCheckPayload)
        SESSION_CAPTURE -> ApiSessionCapturePayload(this as SessionCapturePayload)
        FACE_ONBOARDING_COMPLETE -> ApiFaceOnboardingCompletePayload(this as FaceOnboardingCompletePayload)
        FACE_FALLBACK_CAPTURE -> ApiFaceFallbackCapturePayload(this as FaceFallbackCapturePayload)
        FACE_CAPTURE -> ApiFaceCapturePayload(this as FaceCapturePayload)
        FACE_CAPTURE_CONFIRMATION -> ApiFaceCaptureConfirmationPayload(this as FaceCaptureConfirmationPayload)
        FACE_CAPTURE_RETRY -> ApiFaceCaptureRetryPayload(this as FaceCaptureRetryPayload)
        SCANNER_FIRMWARE_UPDATE -> ApiScannerFirmwareUpdatePayload(this as ScannerFirmwareUpdatePayload)
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
    }
