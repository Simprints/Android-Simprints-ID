package com.simprints.id.data.db.event.remote.models

import com.simprints.id.data.db.event.domain.models.EventType
import com.simprints.id.data.db.event.domain.models.EventType.*


// Unused key field - used to enforce adding a const (key) when a new event is added.
// the key is required to serialise/deserialize events correctly with Jackson (see annotation in ApiEventPayload)
enum class ApiEventPayloadType(val key: String) {

    EnrolmentRecordCreation(ApiEventPayloadType.ENROLMENT_RECORD_CREATION_KEY),
    EnrolmentRecordDeletion(ApiEventPayloadType.ENROLMENT_RECORD_DELETION_KEY),
    EnrolmentRecordMove(ApiEventPayloadType.ENROLMENT_RECORD_MOVE_KEY),
    Callout(ApiEventPayloadType.CALLOUT_KEY),
    Callback(ApiEventPayloadType.CALLBACK_KEY),
    ArtificialTermination(ApiEventPayloadType.ARTIFICIAL_TERMINATION_KEY),
    Authentication(ApiEventPayloadType.AUTHENTICATION_KEY),
    Consent(ApiEventPayloadType.CONSENT_KEY),
    Enrolment(ApiEventPayloadType.ENROLMENT_KEY),
    Authorization(ApiEventPayloadType.AUTHORIZATION_KEY),
    FingerprintCapture(ApiEventPayloadType.FINGERPRINT_CAPTURE_KEY),
    OneToOneMatch(ApiEventPayloadType.ONE_TO_ONE_MATCH_KEY),
    OneToManyMatch(ApiEventPayloadType.ONE_TO_MANY_MATCH_KEY),
    PersonCreation(ApiEventPayloadType.PERSON_CREATION_KEY),
    AlertScreen(ApiEventPayloadType.ALERT_SCREEN_KEY),
    GuidSelection(ApiEventPayloadType.GUID_SELECTION_KEY),
    ConnectivitySnapshot(ApiEventPayloadType.CONNECTIVITY_SNAPSHOT_KEY),
    Refusal(ApiEventPayloadType.REFUSAL_KEY),
    CandidateRead(ApiEventPayloadType.CANDIDATE_READ_KEY),
    ScannerConnection(ApiEventPayloadType.SCANNER_CONNECTION_KEY),
    Vero2InfoSnapshot(ApiEventPayloadType.VERO_2_INFO_SNAPSHOT_KEY),
    ScannerFirmwareUpdate(ApiEventPayloadType.SCANNER_FIRMWARE_UPDATE_KEY),
    InvalidIntent(ApiEventPayloadType.INVALID_INTENT_KEY),
    SuspiciousIntent(ApiEventPayloadType.SUSPICIOUS_INTENT_KEY),
    IntentParsing(ApiEventPayloadType.INTENT_PARSING_KEY),
    CompletionCheck(ApiEventPayloadType.COMPLETION_CHECK_KEY),
    SessionCapture(ApiEventPayloadType.SESSION_CAPTURE_KEY),
    FaceOnboardingComplete(ApiEventPayloadType.FACE_ONBOARDING_COMPLETE_KEY),
    FaceFallbackCapture(ApiEventPayloadType.FACE_FALLBACK_CAPTURE_KEY),
    FaceCapture(ApiEventPayloadType.FACE_CAPTURE_KEY),
    FaceCaptureConfirmation(ApiEventPayloadType.FACE_CAPTURE_CONFIRMATION_KEY),
    FaceCaptureRetry(ApiEventPayloadType.FACE_CAPTURE_RETRY_KEY);

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
