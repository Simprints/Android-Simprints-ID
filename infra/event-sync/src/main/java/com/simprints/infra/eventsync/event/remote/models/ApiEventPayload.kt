package com.simprints.infra.eventsync.event.remote.models

import androidx.annotation.Keep
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
import com.simprints.infra.events.event.domain.models.EventType.CALLOUT_CONFIRMATION_V3
import com.simprints.infra.events.event.domain.models.EventType.CALLOUT_ENROLMENT
import com.simprints.infra.events.event.domain.models.EventType.CALLOUT_ENROLMENT_V3
import com.simprints.infra.events.event.domain.models.EventType.CALLOUT_IDENTIFICATION
import com.simprints.infra.events.event.domain.models.EventType.CALLOUT_IDENTIFICATION_V3
import com.simprints.infra.events.event.domain.models.EventType.CALLOUT_LAST_BIOMETRICS
import com.simprints.infra.events.event.domain.models.EventType.CALLOUT_LAST_BIOMETRICS_V3
import com.simprints.infra.events.event.domain.models.EventType.CALLOUT_VERIFICATION
import com.simprints.infra.events.event.domain.models.EventType.CALLOUT_VERIFICATION_V3
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
import com.simprints.infra.events.event.domain.models.EventType.SAMPLE_UP_SYNC_REQUEST
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
import com.simprints.infra.events.event.domain.models.callout.ConfirmationCalloutEventV2
import com.simprints.infra.events.event.domain.models.callout.ConfirmationCalloutEventV3
import com.simprints.infra.events.event.domain.models.callout.EnrolmentCalloutEventV2
import com.simprints.infra.events.event.domain.models.callout.EnrolmentCalloutEventV3
import com.simprints.infra.events.event.domain.models.callout.EnrolmentLastBiometricsCalloutEventV2
import com.simprints.infra.events.event.domain.models.callout.EnrolmentLastBiometricsCalloutEventV3
import com.simprints.infra.events.event.domain.models.callout.IdentificationCalloutEventV2
import com.simprints.infra.events.event.domain.models.callout.IdentificationCalloutEventV3
import com.simprints.infra.events.event.domain.models.callout.VerificationCalloutEventV2
import com.simprints.infra.events.event.domain.models.callout.VerificationCalloutEventV3
import com.simprints.infra.events.event.domain.models.downsync.EventDownSyncRequestEvent
import com.simprints.infra.events.event.domain.models.face.FaceCaptureBiometricsEvent
import com.simprints.infra.events.event.domain.models.face.FaceCaptureConfirmationEvent.FaceCaptureConfirmationPayload
import com.simprints.infra.events.event.domain.models.face.FaceCaptureEvent
import com.simprints.infra.events.event.domain.models.face.FaceFallbackCaptureEvent.FaceFallbackCapturePayload
import com.simprints.infra.events.event.domain.models.face.FaceOnboardingCompleteEvent.FaceOnboardingCompletePayload
import com.simprints.infra.events.event.domain.models.fingerprint.FingerprintCaptureBiometricsEvent.FingerprintCaptureBiometricsPayload
import com.simprints.infra.events.event.domain.models.fingerprint.FingerprintCaptureEvent
import com.simprints.infra.events.event.domain.models.samples.SampleUpSyncRequestEvent
import com.simprints.infra.events.event.domain.models.upsync.EventUpSyncRequestEvent
import com.simprints.infra.eventsync.event.remote.models.callback.ApiCallbackPayload
import com.simprints.infra.eventsync.event.remote.models.callout.ApiCalloutPayloadV2
import com.simprints.infra.eventsync.event.remote.models.callout.ApiCalloutPayloadV3
import com.simprints.infra.eventsync.event.remote.models.downsync.ApiEventDownSyncRequestPayload
import com.simprints.infra.eventsync.event.remote.models.face.ApiFaceCaptureBiometricsPayload
import com.simprints.infra.eventsync.event.remote.models.face.ApiFaceCaptureConfirmationPayload
import com.simprints.infra.eventsync.event.remote.models.face.ApiFaceCapturePayload
import com.simprints.infra.eventsync.event.remote.models.face.ApiFaceFallbackCapturePayload
import com.simprints.infra.eventsync.event.remote.models.face.ApiFaceOnboardingCompletePayload
import com.simprints.infra.eventsync.event.remote.models.samples.ApiEventSampleUpSyncRequestPayload
import com.simprints.infra.eventsync.event.remote.models.upsync.ApiEventUpSyncRequestPayload

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
    CALLOUT_CONFIRMATION -> ApiCalloutPayloadV2(this as ConfirmationCalloutEventV2.ConfirmationCalloutPayload)
    CALLOUT_CONFIRMATION_V3 -> ApiCalloutPayloadV3(this as ConfirmationCalloutEventV3.ConfirmationCalloutPayload)
    CALLOUT_IDENTIFICATION -> ApiCalloutPayloadV2(this as IdentificationCalloutEventV2.IdentificationCalloutPayload)
    CALLOUT_IDENTIFICATION_V3 -> ApiCalloutPayloadV3(this as IdentificationCalloutEventV3.IdentificationCalloutPayload)
    CALLOUT_ENROLMENT -> ApiCalloutPayloadV2(this as EnrolmentCalloutEventV2.EnrolmentCalloutPayload)
    CALLOUT_ENROLMENT_V3 -> ApiCalloutPayloadV3(this as EnrolmentCalloutEventV3.EnrolmentCalloutPayload)
    CALLOUT_VERIFICATION -> ApiCalloutPayloadV2(this as VerificationCalloutEventV2.VerificationCalloutPayload)
    CALLOUT_VERIFICATION_V3 -> ApiCalloutPayloadV3(this as VerificationCalloutEventV3.VerificationCalloutPayload)
    CALLOUT_LAST_BIOMETRICS -> ApiCalloutPayloadV2(this as EnrolmentLastBiometricsCalloutEventV2.EnrolmentLastBiometricsCalloutPayload)
    CALLOUT_LAST_BIOMETRICS_V3 -> ApiCalloutPayloadV3(this as EnrolmentLastBiometricsCalloutEventV3.EnrolmentLastBiometricsCalloutPayload)
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
    SAMPLE_UP_SYNC_REQUEST -> ApiEventSampleUpSyncRequestPayload(this as SampleUpSyncRequestEvent.SampleUpSyncRequestPayload)
    LICENSE_CHECK -> ApiLicenseCheckEventPayload(this as LicenseCheckEvent.LicenseCheckEventPayload)
    AGE_GROUP_SELECTION -> ApiAgeGroupSelectionPayload(this as AgeGroupSelectionEvent.AgeGroupSelectionPayload)
    BIOMETRIC_REFERENCE_CREATION -> ApiBiometricReferenceCreationPayload(this as BiometricReferenceCreationPayload)
}
