package com.simprints.infra.eventsync.event.remote.models

import androidx.annotation.Keep
import com.simprints.infra.events.event.domain.models.EventType
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
import com.simprints.infra.events.event.domain.models.EventType.ENROLMENT_UPDATE
import com.simprints.infra.events.event.domain.models.EventType.ENROLMENT_V2
import com.simprints.infra.events.event.domain.models.EventType.ENROLMENT_V4
import com.simprints.infra.events.event.domain.models.EventType.EVENT_DOWN_SYNC_REQUEST
import com.simprints.infra.events.event.domain.models.EventType.EVENT_UP_SYNC_REQUEST
import com.simprints.infra.events.event.domain.models.EventType.EXTERNAL_CREDENTIAL_CAPTURE
import com.simprints.infra.events.event.domain.models.EventType.EXTERNAL_CREDENTIAL_CAPTURE_VALUE
import com.simprints.infra.events.event.domain.models.EventType.EXTERNAL_CREDENTIAL_CONFIRMATION
import com.simprints.infra.events.event.domain.models.EventType.EXTERNAL_CREDENTIAL_SEARCH
import com.simprints.infra.events.event.domain.models.EventType.EXTERNAL_CREDENTIAL_SELECTION
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

@Keep
internal enum class ApiEventPayloadType {
    Callout,
    Callback,
    Authentication,
    Consent,
    Enrolment,
    Authorization,
    FingerprintCapture,
    FingerprintCaptureBiometrics,
    OneToOneMatch,
    OneToManyMatch,
    PersonCreation,
    AlertScreen,
    GuidSelection,
    ConnectivitySnapshot,
    Refusal,
    CandidateRead,
    ScannerConnection,
    Vero2InfoSnapshot,
    ScannerFirmwareUpdate,
    InvalidIntent,
    SuspiciousIntent,
    IntentParsing,
    CompletionCheck,
    FaceOnboardingComplete,
    FaceFallbackCapture,
    FaceCapture,
    FaceCaptureBiometrics,
    FaceCaptureConfirmation,
    EventDownSyncRequest,
    EventUpSyncRequest,
    SampleUpSyncRequest,
    LicenseCheck,
    AgeGroupSelection,
    BiometricReferenceCreation,
    EnrolmentUpdate,
    ExternalCredentialSelection,
    ExternalCredentialCaptureValue,
    ExternalCredentialCapture,
    ExternalCredentialSearch,
    ExternalCredentialConfirmation,
}

internal fun EventType.fromDomainToApi(): ApiEventPayloadType = when (this) {
    AUTHENTICATION -> ApiEventPayloadType.Authentication
    CONSENT -> ApiEventPayloadType.Consent
    ENROLMENT_V2, ENROLMENT_V4 -> ApiEventPayloadType.Enrolment
    AUTHORIZATION -> ApiEventPayloadType.Authorization
    FINGERPRINT_CAPTURE -> ApiEventPayloadType.FingerprintCapture
    ONE_TO_ONE_MATCH -> ApiEventPayloadType.OneToOneMatch
    ONE_TO_MANY_MATCH -> ApiEventPayloadType.OneToManyMatch
    PERSON_CREATION -> ApiEventPayloadType.PersonCreation
    ALERT_SCREEN -> ApiEventPayloadType.AlertScreen
    GUID_SELECTION -> ApiEventPayloadType.GuidSelection
    CONNECTIVITY_SNAPSHOT -> ApiEventPayloadType.ConnectivitySnapshot
    REFUSAL -> ApiEventPayloadType.Refusal
    CANDIDATE_READ -> ApiEventPayloadType.CandidateRead
    SCANNER_CONNECTION -> ApiEventPayloadType.ScannerConnection
    VERO_2_INFO_SNAPSHOT -> ApiEventPayloadType.Vero2InfoSnapshot
    SCANNER_FIRMWARE_UPDATE -> ApiEventPayloadType.ScannerFirmwareUpdate
    INVALID_INTENT -> ApiEventPayloadType.InvalidIntent
    CALLOUT_CONFIRMATION,
    CALLOUT_CONFIRMATION_V3,
    CALLOUT_IDENTIFICATION,
    CALLOUT_IDENTIFICATION_V3,
    CALLOUT_ENROLMENT,
    CALLOUT_ENROLMENT_V3,
    CALLOUT_VERIFICATION,
    CALLOUT_VERIFICATION_V3,
    CALLOUT_LAST_BIOMETRICS,
    CALLOUT_LAST_BIOMETRICS_V3,
    -> ApiEventPayloadType.Callout

    CALLBACK_IDENTIFICATION,
    CALLBACK_ENROLMENT,
    CALLBACK_REFUSAL,
    CALLBACK_VERIFICATION,
    CALLBACK_CONFIRMATION,
    CALLBACK_ERROR,
    -> ApiEventPayloadType.Callback

    SUSPICIOUS_INTENT -> ApiEventPayloadType.SuspiciousIntent
    INTENT_PARSING -> ApiEventPayloadType.IntentParsing
    COMPLETION_CHECK -> ApiEventPayloadType.CompletionCheck
    FACE_ONBOARDING_COMPLETE -> ApiEventPayloadType.FaceOnboardingComplete
    FACE_FALLBACK_CAPTURE -> ApiEventPayloadType.FaceFallbackCapture
    FACE_CAPTURE -> ApiEventPayloadType.FaceCapture
    FACE_CAPTURE_CONFIRMATION -> ApiEventPayloadType.FaceCaptureConfirmation
    FINGERPRINT_CAPTURE_BIOMETRICS -> ApiEventPayloadType.FingerprintCaptureBiometrics
    FACE_CAPTURE_BIOMETRICS -> ApiEventPayloadType.FaceCaptureBiometrics
    EVENT_DOWN_SYNC_REQUEST -> ApiEventPayloadType.EventDownSyncRequest
    EVENT_UP_SYNC_REQUEST -> ApiEventPayloadType.EventUpSyncRequest
    SAMPLE_UP_SYNC_REQUEST -> ApiEventPayloadType.SampleUpSyncRequest
    LICENSE_CHECK -> ApiEventPayloadType.LicenseCheck
    AGE_GROUP_SELECTION -> ApiEventPayloadType.AgeGroupSelection
    BIOMETRIC_REFERENCE_CREATION -> ApiEventPayloadType.BiometricReferenceCreation
    ENROLMENT_UPDATE -> ApiEventPayloadType.EnrolmentUpdate
    EXTERNAL_CREDENTIAL_SELECTION -> ApiEventPayloadType.ExternalCredentialSelection
    EXTERNAL_CREDENTIAL_CAPTURE_VALUE -> ApiEventPayloadType.ExternalCredentialCaptureValue
    EXTERNAL_CREDENTIAL_CAPTURE -> ApiEventPayloadType.ExternalCredentialCapture
    EXTERNAL_CREDENTIAL_SEARCH -> ApiEventPayloadType.ExternalCredentialSearch
    EXTERNAL_CREDENTIAL_CONFIRMATION -> ApiEventPayloadType.ExternalCredentialConfirmation
}

internal fun ApiEventPayloadType.fromApiToDomain(): EventType = when (this) {
    ApiEventPayloadType.Authentication -> AUTHENTICATION
    ApiEventPayloadType.Consent -> CONSENT
    ApiEventPayloadType.Enrolment -> ENROLMENT_V4
    ApiEventPayloadType.Authorization -> AUTHORIZATION
    ApiEventPayloadType.FingerprintCapture -> FINGERPRINT_CAPTURE
    ApiEventPayloadType.OneToOneMatch -> ONE_TO_MANY_MATCH
    ApiEventPayloadType.OneToManyMatch -> ONE_TO_MANY_MATCH
    ApiEventPayloadType.PersonCreation -> PERSON_CREATION
    ApiEventPayloadType.AlertScreen -> ALERT_SCREEN
    ApiEventPayloadType.GuidSelection -> GUID_SELECTION
    ApiEventPayloadType.ConnectivitySnapshot -> CONNECTIVITY_SNAPSHOT
    ApiEventPayloadType.Refusal -> REFUSAL
    ApiEventPayloadType.CandidateRead -> CANDIDATE_READ
    ApiEventPayloadType.ScannerConnection -> SCANNER_CONNECTION
    ApiEventPayloadType.Vero2InfoSnapshot -> VERO_2_INFO_SNAPSHOT
    ApiEventPayloadType.ScannerFirmwareUpdate -> SCANNER_FIRMWARE_UPDATE
    ApiEventPayloadType.InvalidIntent -> INVALID_INTENT
    ApiEventPayloadType.SuspiciousIntent -> SUSPICIOUS_INTENT
    ApiEventPayloadType.IntentParsing -> INTENT_PARSING
    ApiEventPayloadType.CompletionCheck -> COMPLETION_CHECK
    ApiEventPayloadType.FaceOnboardingComplete -> FACE_ONBOARDING_COMPLETE
    ApiEventPayloadType.FaceFallbackCapture -> FACE_FALLBACK_CAPTURE
    ApiEventPayloadType.FaceCapture -> FACE_CAPTURE
    ApiEventPayloadType.FaceCaptureConfirmation -> FACE_CAPTURE_CONFIRMATION
    ApiEventPayloadType.FingerprintCaptureBiometrics -> FINGERPRINT_CAPTURE_BIOMETRICS
    ApiEventPayloadType.FaceCaptureBiometrics -> FACE_CAPTURE_BIOMETRICS
    ApiEventPayloadType.EventDownSyncRequest -> EVENT_DOWN_SYNC_REQUEST
    ApiEventPayloadType.EventUpSyncRequest -> EVENT_UP_SYNC_REQUEST
    ApiEventPayloadType.SampleUpSyncRequest -> SAMPLE_UP_SYNC_REQUEST
    ApiEventPayloadType.LicenseCheck -> LICENSE_CHECK
    ApiEventPayloadType.AgeGroupSelection -> AGE_GROUP_SELECTION
    ApiEventPayloadType.BiometricReferenceCreation -> BIOMETRIC_REFERENCE_CREATION
    ApiEventPayloadType.Callout -> throw UnsupportedOperationException("")
    ApiEventPayloadType.Callback -> throw UnsupportedOperationException("")
    ApiEventPayloadType.EnrolmentUpdate -> ENROLMENT_UPDATE
    ApiEventPayloadType.ExternalCredentialSelection -> EXTERNAL_CREDENTIAL_SELECTION
    ApiEventPayloadType.ExternalCredentialCaptureValue -> EXTERNAL_CREDENTIAL_CAPTURE_VALUE
    ApiEventPayloadType.ExternalCredentialCapture -> EXTERNAL_CREDENTIAL_CAPTURE
    ApiEventPayloadType.ExternalCredentialSearch -> EXTERNAL_CREDENTIAL_SEARCH
    ApiEventPayloadType.ExternalCredentialConfirmation -> EXTERNAL_CREDENTIAL_CONFIRMATION
}
