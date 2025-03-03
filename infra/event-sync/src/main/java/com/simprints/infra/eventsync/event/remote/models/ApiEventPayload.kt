package com.simprints.infra.eventsync.event.remote.models

import androidx.annotation.Keep
import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.simprints.infra.config.store.models.TokenKeyType
import com.simprints.infra.events.event.domain.models.AgeGroupSelectionEvent
import com.simprints.infra.events.event.domain.models.AlertScreenEvent.AlertScreenPayload
import com.simprints.infra.events.event.domain.models.AuthenticationEvent.AuthenticationPayload
import com.simprints.infra.events.event.domain.models.AuthorizationEvent.AuthorizationPayload
import com.simprints.infra.events.event.domain.models.BiometricReferenceCreationEvent.BiometricReferenceCreationPayload
import com.simprints.infra.events.event.domain.models.CandidateReadEvent.CandidateReadPayload
import com.simprints.infra.events.event.domain.models.CompletionCheckEvent.CompletionCheckPayload
import com.simprints.infra.events.event.domain.models.ConnectivitySnapshotEvent.ConnectivitySnapshotPayload
import com.simprints.infra.events.event.domain.models.ConsentEvent.ConsentPayload
import com.simprints.infra.events.event.domain.models.EnrolmentEventV2
import com.simprints.infra.events.event.domain.models.EnrolmentEventV4
import com.simprints.infra.events.event.domain.models.EventPayload
import com.simprints.infra.events.event.domain.models.EventType.AGE_GROUP_SELECTION
import com.simprints.infra.events.event.domain.models.EventType.ALERT_SCREEN
import com.simprints.infra.events.event.domain.models.EventType.AUTHENTICATION
import com.simprints.infra.events.event.domain.models.EventType.AUTHORIZATION
import com.simprints.infra.events.event.domain.models.EventType.BIOMETRIC_REFERENCE_CREATION
import com.simprints.infra.events.event.domain.models.EventType.CALLBACK_CONFIRMATION
import com.simprints.infra.events.event.domain.models.EventType.CALLBACK_ENROLMENT
import com.simprints.infra.events.event.domain.models.EventType.CALLBACK_ERROR
import com.simprints.infra.events.event.domain.models.EventType.CALLBACK_IDENTIFICATION
import com.simprints.infra.events.event.domain.models.EventType.CALLBACK_REFUSAL
import com.simprints.infra.events.event.domain.models.EventType.CALLBACK_VERIFICATION
import com.simprints.infra.events.event.domain.models.EventType.CALLOUT_CONFIRMATION
import com.simprints.infra.events.event.domain.models.EventType.CALLOUT_ENROLMENT
import com.simprints.infra.events.event.domain.models.EventType.CALLOUT_IDENTIFICATION
import com.simprints.infra.events.event.domain.models.EventType.CALLOUT_LAST_BIOMETRICS
import com.simprints.infra.events.event.domain.models.EventType.CALLOUT_VERIFICATION
import com.simprints.infra.events.event.domain.models.EventType.CANDIDATE_READ
import com.simprints.infra.events.event.domain.models.EventType.COMPLETION_CHECK
import com.simprints.infra.events.event.domain.models.EventType.CONNECTIVITY_SNAPSHOT
import com.simprints.infra.events.event.domain.models.EventType.CONSENT
import com.simprints.infra.events.event.domain.models.EventType.ENROLMENT_V2
import com.simprints.infra.events.event.domain.models.EventType.ENROLMENT_V4
import com.simprints.infra.events.event.domain.models.EventType.EVENT_DOWN_SYNC_REQUEST
import com.simprints.infra.events.event.domain.models.EventType.EVENT_UP_SYNC_REQUEST
import com.simprints.infra.events.event.domain.models.EventType.FACE_CAPTURE
import com.simprints.infra.events.event.domain.models.EventType.FACE_CAPTURE_BIOMETRICS
import com.simprints.infra.events.event.domain.models.EventType.FACE_CAPTURE_CONFIRMATION
import com.simprints.infra.events.event.domain.models.EventType.FACE_FALLBACK_CAPTURE
import com.simprints.infra.events.event.domain.models.EventType.FACE_ONBOARDING_COMPLETE
import com.simprints.infra.events.event.domain.models.EventType.FINGERPRINT_CAPTURE
import com.simprints.infra.events.event.domain.models.EventType.FINGERPRINT_CAPTURE_BIOMETRICS
import com.simprints.infra.events.event.domain.models.EventType.GUID_SELECTION
import com.simprints.infra.events.event.domain.models.EventType.INTENT_PARSING
import com.simprints.infra.events.event.domain.models.EventType.INVALID_INTENT
import com.simprints.infra.events.event.domain.models.EventType.LICENSE_CHECK
import com.simprints.infra.events.event.domain.models.EventType.ONE_TO_MANY_MATCH
import com.simprints.infra.events.event.domain.models.EventType.ONE_TO_ONE_MATCH
import com.simprints.infra.events.event.domain.models.EventType.PERSON_CREATION
import com.simprints.infra.events.event.domain.models.EventType.REFUSAL
import com.simprints.infra.events.event.domain.models.EventType.SCANNER_CONNECTION
import com.simprints.infra.events.event.domain.models.EventType.SCANNER_FIRMWARE_UPDATE
import com.simprints.infra.events.event.domain.models.EventType.SUSPICIOUS_INTENT
import com.simprints.infra.events.event.domain.models.EventType.VERO_2_INFO_SNAPSHOT
import com.simprints.infra.events.event.domain.models.GuidSelectionEvent.GuidSelectionPayload
import com.simprints.infra.events.event.domain.models.IntentParsingEvent.IntentParsingPayload
import com.simprints.infra.events.event.domain.models.InvalidIntentEvent.InvalidIntentPayload
import com.simprints.infra.events.event.domain.models.LicenseCheckEvent
import com.simprints.infra.events.event.domain.models.OneToManyMatchEvent.OneToManyMatchPayload
import com.simprints.infra.events.event.domain.models.OneToOneMatchEvent.OneToOneMatchPayload
import com.simprints.infra.events.event.domain.models.PersonCreationEvent.PersonCreationPayload
import com.simprints.infra.events.event.domain.models.RefusalEvent.RefusalPayload
import com.simprints.infra.events.event.domain.models.ScannerConnectionEvent.ScannerConnectionPayload
import com.simprints.infra.events.event.domain.models.ScannerFirmwareUpdateEvent.ScannerFirmwareUpdatePayload
import com.simprints.infra.events.event.domain.models.SuspiciousIntentEvent.SuspiciousIntentPayload
import com.simprints.infra.events.event.domain.models.Vero2InfoSnapshotEvent.Vero2InfoSnapshotPayload
import com.simprints.infra.events.event.domain.models.callback.ConfirmationCallbackEvent.ConfirmationCallbackPayload
import com.simprints.infra.events.event.domain.models.callback.EnrolmentCallbackEvent.EnrolmentCallbackPayload
import com.simprints.infra.events.event.domain.models.callback.ErrorCallbackEvent.ErrorCallbackPayload
import com.simprints.infra.events.event.domain.models.callback.IdentificationCallbackEvent.IdentificationCallbackPayload
import com.simprints.infra.events.event.domain.models.callback.RefusalCallbackEvent.RefusalCallbackPayload
import com.simprints.infra.events.event.domain.models.callback.VerificationCallbackEvent.VerificationCallbackPayload
import com.simprints.infra.events.event.domain.models.callout.ConfirmationCalloutEvent.ConfirmationCalloutPayload
import com.simprints.infra.events.event.domain.models.callout.EnrolmentCalloutEvent.EnrolmentCalloutPayload
import com.simprints.infra.events.event.domain.models.callout.EnrolmentLastBiometricsCalloutEvent.EnrolmentLastBiometricsCalloutPayload
import com.simprints.infra.events.event.domain.models.callout.IdentificationCalloutEvent.IdentificationCalloutPayload
import com.simprints.infra.events.event.domain.models.callout.VerificationCalloutEvent.VerificationCalloutPayload
import com.simprints.infra.events.event.domain.models.downsync.EventDownSyncRequestEvent
import com.simprints.infra.events.event.domain.models.face.FaceCaptureBiometricsEvent
import com.simprints.infra.events.event.domain.models.face.FaceCaptureConfirmationEvent.FaceCaptureConfirmationPayload
import com.simprints.infra.events.event.domain.models.face.FaceCaptureEvent
import com.simprints.infra.events.event.domain.models.face.FaceFallbackCaptureEvent.FaceFallbackCapturePayload
import com.simprints.infra.events.event.domain.models.face.FaceOnboardingCompleteEvent.FaceOnboardingCompletePayload
import com.simprints.infra.events.event.domain.models.fingerprint.FingerprintCaptureBiometricsEvent.FingerprintCaptureBiometricsPayload
import com.simprints.infra.events.event.domain.models.fingerprint.FingerprintCaptureEvent
import com.simprints.infra.events.event.domain.models.upsync.EventUpSyncRequestEvent
import com.simprints.infra.eventsync.event.remote.models.ApiEventPayloadType.Companion
import com.simprints.infra.eventsync.event.remote.models.callback.ApiCallbackPayload
import com.simprints.infra.eventsync.event.remote.models.callout.ApiCalloutPayload
import com.simprints.infra.eventsync.event.remote.models.downsync.ApiEventDownSyncRequestPayload
import com.simprints.infra.eventsync.event.remote.models.face.ApiFaceCaptureBiometricsPayload
import com.simprints.infra.eventsync.event.remote.models.face.ApiFaceCaptureConfirmationPayload
import com.simprints.infra.eventsync.event.remote.models.face.ApiFaceCapturePayload
import com.simprints.infra.eventsync.event.remote.models.face.ApiFaceFallbackCapturePayload
import com.simprints.infra.eventsync.event.remote.models.face.ApiFaceOnboardingCompletePayload
import com.simprints.infra.eventsync.event.remote.models.upsync.ApiEventUpSyncRequestPayload

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "type", visible = true)
@JsonSubTypes(
    JsonSubTypes.Type(value = ApiFaceCaptureConfirmationPayload::class, name = Companion.FACE_CAPTURE_CONFIRMATION_KEY),
    JsonSubTypes.Type(value = ApiFaceCapturePayload::class, name = Companion.FACE_CAPTURE_KEY),
    JsonSubTypes.Type(value = ApiFaceCaptureBiometricsPayload::class, name = Companion.FACE_CAPTURE_BIOMETRICS_KEY),
    JsonSubTypes.Type(value = ApiFaceFallbackCapturePayload::class, name = Companion.FACE_FALLBACK_CAPTURE_KEY),
    JsonSubTypes.Type(value = ApiFaceOnboardingCompletePayload::class, name = Companion.FACE_ONBOARDING_COMPLETE_KEY),
    JsonSubTypes.Type(value = ApiAlertScreenPayload::class, name = Companion.ALERT_SCREEN_KEY),
    JsonSubTypes.Type(value = ApiAuthenticationPayload::class, name = Companion.AUTHENTICATION_KEY),
    JsonSubTypes.Type(value = ApiAuthorizationPayload::class, name = Companion.AUTHORIZATION_KEY),
    JsonSubTypes.Type(value = ApiCandidateReadPayload::class, name = Companion.CANDIDATE_READ_KEY),
    JsonSubTypes.Type(value = ApiCompletionCheckPayload::class, name = Companion.COMPLETION_CHECK_KEY),
    JsonSubTypes.Type(value = ApiConnectivitySnapshotPayload::class, name = Companion.CONNECTIVITY_SNAPSHOT_KEY),
    JsonSubTypes.Type(value = ApiConsentPayload::class, name = Companion.CONSENT_KEY),
    JsonSubTypes.Type(value = ApiEnrolmentPayloadV4::class, name = Companion.ENROLMENT_KEY),
    JsonSubTypes.Type(value = ApiFingerprintCapturePayload::class, name = Companion.FINGERPRINT_CAPTURE_KEY),
    JsonSubTypes.Type(value = ApiFingerprintCaptureBiometricsPayload::class, name = Companion.FINGERPRINT_CAPTURE_BIOMETRICS_KEY),
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
    JsonSubTypes.Type(value = ApiCalloutPayload::class, name = Companion.CALLBACK_KEY),
    JsonSubTypes.Type(value = ApiEventDownSyncRequestPayload::class, name = Companion.EVENT_DOWN_SYNC_REQUEST_KEY),
    JsonSubTypes.Type(value = ApiEventUpSyncRequestPayload::class, name = Companion.EVENT_UP_SYNC_REQUEST_KEY),
    JsonSubTypes.Type(value = ApiBiometricReferenceCreationPayload::class, name = Companion.BIOMETRIC_REFERENCE_CREATION_KEY),
)
@Keep
internal abstract class ApiEventPayload(
    open val startTime: ApiTimestamp,
) {
    abstract fun getTokenizedFieldJsonPath(tokenKeyType: TokenKeyType): String?
}

internal fun EventPayload.fromDomainToApi(): ApiEventPayload = when (this.type) {
    AUTHENTICATION -> ApiAuthenticationPayload(this as AuthenticationPayload)
    CONSENT -> ApiConsentPayload(this as ConsentPayload)
    ENROLMENT_V2 -> ApiEnrolmentPayloadV2(this as EnrolmentEventV2.EnrolmentPayload)
    ENROLMENT_V4 -> ApiEnrolmentPayloadV4(this as EnrolmentEventV4.EnrolmentPayload)
    AUTHORIZATION -> ApiAuthorizationPayload(this as AuthorizationPayload)
    FINGERPRINT_CAPTURE -> ApiFingerprintCapturePayload(this as FingerprintCaptureEvent.FingerprintCapturePayload)
    ONE_TO_ONE_MATCH -> ApiOneToOneMatchPayload(this as OneToOneMatchPayload)
    ONE_TO_MANY_MATCH -> ApiOneToManyMatchPayload(this as OneToManyMatchPayload)
    PERSON_CREATION -> ApiPersonCreationPayload(this as PersonCreationPayload)
    ALERT_SCREEN -> ApiAlertScreenPayload(this as AlertScreenPayload)
    GUID_SELECTION -> ApiGuidSelectionPayload(this as GuidSelectionPayload)
    CONNECTIVITY_SNAPSHOT -> ApiConnectivitySnapshotPayload(this as ConnectivitySnapshotPayload)
    REFUSAL -> ApiRefusalPayload(this as RefusalPayload)
    CANDIDATE_READ -> ApiCandidateReadPayload(this as CandidateReadPayload)
    SCANNER_CONNECTION -> ApiScannerConnectionPayload(this as ScannerConnectionPayload)
    VERO_2_INFO_SNAPSHOT -> toApiVero2InfoSnapshotPayload(this as Vero2InfoSnapshotPayload)
    INVALID_INTENT -> ApiInvalidIntentPayload(this as InvalidIntentPayload)
    SUSPICIOUS_INTENT -> ApiSuspiciousIntentPayload(this as SuspiciousIntentPayload)
    INTENT_PARSING -> ApiIntentParsingPayload(this as IntentParsingPayload)
    COMPLETION_CHECK -> ApiCompletionCheckPayload(this as CompletionCheckPayload)
    FACE_ONBOARDING_COMPLETE -> ApiFaceOnboardingCompletePayload(this as FaceOnboardingCompletePayload)
    FACE_FALLBACK_CAPTURE -> ApiFaceFallbackCapturePayload(this as FaceFallbackCapturePayload)
    FACE_CAPTURE -> ApiFaceCapturePayload(this as FaceCaptureEvent.FaceCapturePayload)
    FACE_CAPTURE_CONFIRMATION -> ApiFaceCaptureConfirmationPayload(this as FaceCaptureConfirmationPayload)
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
    FINGERPRINT_CAPTURE_BIOMETRICS -> ApiFingerprintCaptureBiometricsPayload(this as FingerprintCaptureBiometricsPayload)
    FACE_CAPTURE_BIOMETRICS -> ApiFaceCaptureBiometricsPayload(this as FaceCaptureBiometricsEvent.FaceCaptureBiometricsPayload)
    EVENT_DOWN_SYNC_REQUEST -> ApiEventDownSyncRequestPayload(this as EventDownSyncRequestEvent.EventDownSyncRequestPayload)
    EVENT_UP_SYNC_REQUEST -> ApiEventUpSyncRequestPayload(this as EventUpSyncRequestEvent.EventUpSyncRequestPayload)
    LICENSE_CHECK -> ApiLicenseCheckEventPayload(this as LicenseCheckEvent.LicenseCheckEventPayload)
    AGE_GROUP_SELECTION -> ApiAgeGroupSelectionPayload(this as AgeGroupSelectionEvent.AgeGroupSelectionPayload)
    BIOMETRIC_REFERENCE_CREATION -> ApiBiometricReferenceCreationPayload(this as BiometricReferenceCreationPayload)
}
