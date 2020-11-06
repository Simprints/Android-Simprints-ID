package com.simprints.id.data.db.event.remote.models

import com.simprints.id.data.db.event.domain.models.EventType
import com.simprints.id.data.db.event.domain.models.EventType.*


enum class ApiEventPayloadType {

    // a constant key is required to serialise/deserialize
    // ApiEventPayload correctly with Jackson (see annotation in ApiEventPayload).
    // Add a key in the companion object for each enum value

    /* key added: ENROLMENT_RECORD_CREATION_KEY */
    EnrolmentRecordCreation,
    /* key added: ENROLMENT_RECORD_DELETION_KEY */
    EnrolmentRecordDeletion,
    /* key added: ENROLMENT_RECORD_MOVE_KEY */
    EnrolmentRecordMove,
    /* key added: CALLOUT_KEY */
    Callout,
    /* key added: CALLBACK_KEY */
    Callback,
    /* key added: ARTIFICIAL_TERMINATION_KEY */
    ArtificialTermination,
    /* key added: AUTHENTICATION_KEY */
    Authentication,
    /* key added: CONSENT_KEY */
    Consent,
    /* key added: ENROLMENT_KEY */
    Enrolment,
    /* key added: AUTHORIZATION_KEY */
    Authorization,
    /* key added: FINGERPRINT_CAPTURE_KEY */
    FingerprintCapture,
    /* key added: ONE_TO_ONE_MATCH_KEY */
    OneToOneMatch,
    /* key added: ONE_TO_MANY_MATCH_KEY */
    OneToManyMatch,
    /* key added: PERSON_CREATION_KEY */
    PersonCreation,
    /* key added: ALERT_SCREEN_KEY */
    AlertScreen,
    /* key added: GUID_SELECTION_KEY */
    GuidSelection,
    /* key added: CONNECTIVITY_SNAPSHOT_KEY */
    ConnectivitySnapshot,
    /* key added: REFUSAL_KEY */
    Refusal,
    /* key added: CANDIDATE_READ_KEY */
    CandidateRead,
    /* key added: SCANNER_CONNECTION_KEY */
    ScannerConnection,
    /* key added: VERO_2_INFO_SNAPSHOT_KEY */
    Vero2InfoSnapshot,
    /* key added: SCANNER_FIRMWARE_UPDATE_KEY */
    ScannerFirmwareUpdate,
    /* key added: INVALID_INTENT_KEY */
    InvalidIntent,
    /* key added: SUSPICIOUS_INTENT_KEY */
    SuspiciousIntent,
    /* key added: INTENT_PARSING_KEY */
    IntentParsing,
    /* key added: COMPLETION_CHECK_KEY */
    CompletionCheck,
    /* key added: SESSION_CAPTURE_KEY */
    SessionCapture,
    /* key added: FACE_ONBOARDING_COMPLETE_KEY */
    FaceOnboardingComplete,
    /* key added: FACE_FALLBACK_CAPTURE_KEY */
    FaceFallbackCapture,
    /* key added: FACE_CAPTURE_KEY */
    FaceCapture,
    /* key added: FACE_CAPTURE_CONFIRMATION_KEY */
    FaceCaptureConfirmation,
    /* key added: FACE_CAPTURE_RETRY_KEY */
    FaceCaptureRetry;

    companion object {
        const val ENROLMENT_RECORD_CREATION_KEY = "EnrolmentRecordCreation"
        const val ENROLMENT_RECORD_DELETION_KEY = "EnrolmentRecordDeletion"
        const val ENROLMENT_RECORD_MOVE_KEY = "EnrolmentRecordMove"
        const val CALLOUT_KEY = "Callout"
        const val CALLBACK_KEY = "Callback"
        const val ARTIFICIAL_TERMINATION_KEY = "ArtificialTermination"
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
        const val SESSION_CAPTURE_KEY = "SessionCapture"
        const val FACE_ONBOARDING_COMPLETE_KEY = "FaceOnboardingComplete"
        const val FACE_FALLBACK_CAPTURE_KEY = "FaceFallbackCapture"
        const val FACE_CAPTURE_KEY = "FaceCapture"
        const val FACE_CAPTURE_CONFIRMATION_KEY = "FaceCaptureConfirmation"
        const val FACE_CAPTURE_RETRY_KEY = "FaceCaptureRetry"
    }
}

fun EventType.fromDomainToApi(): ApiEventPayloadType = when (this) {
    ENROLMENT_RECORD_CREATION -> ApiEventPayloadType.EnrolmentRecordCreation
    ENROLMENT_RECORD_DELETION -> ApiEventPayloadType.EnrolmentRecordDeletion
    ARTIFICIAL_TERMINATION -> ApiEventPayloadType.ArtificialTermination
    AUTHENTICATION -> ApiEventPayloadType.Authentication
    CONSENT -> ApiEventPayloadType.Consent
    ENROLMENT -> ApiEventPayloadType.Enrolment
    AUTHORIZATION -> ApiEventPayloadType.Authorization
    FINGERPRINT_CAPTURE -> ApiEventPayloadType.FingerprintCapture
    ONE_TO_ONE_MATCH -> ApiEventPayloadType.OneToManyMatch
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
    CALLOUT_LAST_BIOMETRICS -> ApiEventPayloadType.Callout
    CALLBACK_IDENTIFICATION,
    CALLBACK_ENROLMENT,
    CALLBACK_REFUSAL,
    CALLBACK_VERIFICATION,
    CALLBACK_CONFIRMATION,
    CALLBACK_ERROR -> ApiEventPayloadType.Callback
    SUSPICIOUS_INTENT -> ApiEventPayloadType.SuspiciousIntent
    INTENT_PARSING -> ApiEventPayloadType.IntentParsing
    COMPLETION_CHECK -> ApiEventPayloadType.CompletionCheck
    SESSION_CAPTURE -> ApiEventPayloadType.SessionCapture
    ENROLMENT_RECORD_MOVE -> ApiEventPayloadType.EnrolmentRecordMove
    FACE_ONBOARDING_COMPLETE -> ApiEventPayloadType.FaceOnboardingComplete
    FACE_FALLBACK_CAPTURE -> ApiEventPayloadType.FaceFallbackCapture
    FACE_CAPTURE -> ApiEventPayloadType.FaceCapture
    FACE_CAPTURE_CONFIRMATION -> ApiEventPayloadType.FaceCaptureConfirmation
    FACE_CAPTURE_RETRY -> ApiEventPayloadType.FaceCaptureRetry
}


fun ApiEventPayloadType.fromApiToDomain(): EventType = when (this) {
    ApiEventPayloadType.EnrolmentRecordCreation -> ENROLMENT_RECORD_CREATION
    ApiEventPayloadType.EnrolmentRecordDeletion -> ENROLMENT_RECORD_DELETION
    ApiEventPayloadType.ArtificialTermination -> ARTIFICIAL_TERMINATION
    ApiEventPayloadType.Authentication -> AUTHENTICATION
    ApiEventPayloadType.Consent -> CONSENT
    ApiEventPayloadType.Enrolment -> ENROLMENT
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
    ApiEventPayloadType.SessionCapture -> SESSION_CAPTURE
    ApiEventPayloadType.EnrolmentRecordMove -> ENROLMENT_RECORD_MOVE
    ApiEventPayloadType.FaceOnboardingComplete -> FACE_ONBOARDING_COMPLETE
    ApiEventPayloadType.FaceFallbackCapture -> FACE_FALLBACK_CAPTURE
    ApiEventPayloadType.FaceCapture -> FACE_CAPTURE
    ApiEventPayloadType.FaceCaptureConfirmation -> FACE_CAPTURE_CONFIRMATION
    ApiEventPayloadType.FaceCaptureRetry -> FACE_CAPTURE_RETRY
    ApiEventPayloadType.Callout -> throw UnsupportedOperationException("")
    ApiEventPayloadType.Callback -> throw UnsupportedOperationException("")
}
