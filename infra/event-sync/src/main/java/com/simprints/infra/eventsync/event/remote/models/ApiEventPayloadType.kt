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

@Keep
internal enum class ApiEventPayloadType {
    // a constant key is required to serialise/deserialize
    // ApiEventPayload correctly with Jackson (see annotation in ApiEventPayload).
    // Add a key in the companion object for each enum value

    // key added: CALLOUT_KEY
    Callout,

    // key added: CALLBACK_KEY
    Callback,

    // key added: AUTHENTICATION_KEY
    Authentication,

    // key added: CONSENT_KEY
    Consent,

    // key added: ENROLMENT_KEY
    Enrolment,

    // key added: AUTHORIZATION_KEY
    Authorization,

    // key added: FINGERPRINT_CAPTURE_KEY
    FingerprintCapture,

    // key added: FINGERPRINT_CAPTURE_BIOMETRICS_KEY
    FingerprintCaptureBiometrics,

    // key added: ONE_TO_ONE_MATCH_KEY
    OneToOneMatch,

    // key added: ONE_TO_MANY_MATCH_KEY
    OneToManyMatch,

    // key added: PERSON_CREATION_KEY
    PersonCreation,

    // key added: ALERT_SCREEN_KEY
    AlertScreen,

    // key added: GUID_SELECTION_KEY
    GuidSelection,

    // key added: CONNECTIVITY_SNAPSHOT_KEY
    ConnectivitySnapshot,

    // key added: REFUSAL_KEY
    Refusal,

    // key added: CANDIDATE_READ_KEY
    CandidateRead,

    // key added: SCANNER_CONNECTION_KEY
    ScannerConnection,

    // key added: VERO_2_INFO_SNAPSHOT_KEY
    Vero2InfoSnapshot,

    // key added: SCANNER_FIRMWARE_UPDATE_KEY
    ScannerFirmwareUpdate,

    // key added: INVALID_INTENT_KEY
    InvalidIntent,

    // key added: SUSPICIOUS_INTENT_KEY
    SuspiciousIntent,

    // key added: INTENT_PARSING_KEY
    IntentParsing,

    // key added: COMPLETION_CHECK_KEY
    CompletionCheck,

    // key added: FACE_ONBOARDING_COMPLETE_KEY
    FaceOnboardingComplete,

    // key added: FACE_FALLBACK_CAPTURE_KEY
    FaceFallbackCapture,

    // key added: FACE_CAPTURE_KEY
    FaceCapture,

    // key added: FACE_CAPTURE_BIOMETRICS_KEY
    FaceCaptureBiometrics,

    // key added: FACE_CAPTURE_CONFIRMATION_KEY
    FaceCaptureConfirmation,

    // key added: EVENT_DOWN_SYNC_REQUEST_KEY
    EventDownSyncRequest,

    // key added: EVENT_UP_SYNC_REQUEST_KEY
    EventUpSyncRequest,

    // key added: LICENSE_CHECK_KEY
    LicenseCheck,

    // key added: AGE_GROUP_SELECTION_KEY
    AgeGroupSelection,

    // key added: BIOMETRIC_REFERENCE_CREATION_KEY
    BiometricReferenceCreation,

    ;

    companion object {
        const val CALLOUT_KEY = "Callout"
        const val CALLBACK_KEY = "Callback"
        const val AUTHENTICATION_KEY = "Authentication"
        const val CONSENT_KEY = "Consent"
        const val ENROLMENT_KEY = "Enrolment"
        const val AUTHORIZATION_KEY = "Authorization"
        const val FINGERPRINT_CAPTURE_KEY = "FingerprintCapture"
        const val ONE_TO_ONE_MATCH_KEY = "OneToOneMatch"
        const val ONE_TO_MANY_MATCH_KEY = "OneToManyMatch"
        const val PERSON_CREATION_KEY = "PersonCreation"
        const val ALERT_SCREEN_KEY = "AlertScreen"
        const val GUID_SELECTION_KEY = "GuidSelection"
        const val CONNECTIVITY_SNAPSHOT_KEY = "ConnectivitySnapshot"
        const val REFUSAL_KEY = "Refusal"
        const val CANDIDATE_READ_KEY = "CandidateRead"
        const val SCANNER_CONNECTION_KEY = "ScannerConnection"
        const val VERO_2_INFO_SNAPSHOT_KEY = "Vero2InfoSnapshot"
        const val SCANNER_FIRMWARE_UPDATE_KEY = "ScannerFirmwareUpdate"
        const val INVALID_INTENT_KEY = "InvalidIntent"
        const val SUSPICIOUS_INTENT_KEY = "SuspiciousIntent"
        const val INTENT_PARSING_KEY = "IntentParsing"
        const val COMPLETION_CHECK_KEY = "CompletionCheck"
        const val FACE_ONBOARDING_COMPLETE_KEY = "FaceOnboardingComplete"
        const val FACE_FALLBACK_CAPTURE_KEY = "FaceFallbackCapture"
        const val FACE_CAPTURE_KEY = "FaceCapture"
        const val FACE_CAPTURE_CONFIRMATION_KEY = "FaceCaptureConfirmation"
        const val FACE_CAPTURE_BIOMETRICS_KEY = "FaceCaptureBiometrics"
        const val FINGERPRINT_CAPTURE_BIOMETRICS_KEY = "FingerprintCaptureBiometrics"
        const val EVENT_DOWN_SYNC_REQUEST_KEY = "EventDownSyncRequest"
        const val EVENT_UP_SYNC_REQUEST_KEY = "EventUpSyncRequest"
        const val BIOMETRIC_REFERENCE_CREATION_KEY = "BiometricReferenceCreation"
    }
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
    CALLOUT_IDENTIFICATION,
    CALLOUT_ENROLMENT,
    CALLOUT_VERIFICATION,
    CALLOUT_LAST_BIOMETRICS,
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
    LICENSE_CHECK -> ApiEventPayloadType.LicenseCheck
    AGE_GROUP_SELECTION -> ApiEventPayloadType.AgeGroupSelection
    BIOMETRIC_REFERENCE_CREATION -> ApiEventPayloadType.BiometricReferenceCreation
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
    ApiEventPayloadType.LicenseCheck -> LICENSE_CHECK
    ApiEventPayloadType.AgeGroupSelection -> AGE_GROUP_SELECTION
    ApiEventPayloadType.BiometricReferenceCreation -> BIOMETRIC_REFERENCE_CREATION
    ApiEventPayloadType.Callout -> throw UnsupportedOperationException("")
    ApiEventPayloadType.Callback -> throw UnsupportedOperationException("")
}
