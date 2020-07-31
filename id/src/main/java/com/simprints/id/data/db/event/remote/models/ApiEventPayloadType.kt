package com.simprints.id.data.db.event.remote.models

import com.simprints.id.data.db.event.domain.models.EventType
import com.simprints.id.data.db.event.domain.models.EventType.*

enum class ApiEventPayloadType(val key: String) {
    ENROLMENT_RECORD_CREATION(ApiEventPayloadType.ENROLMENT_RECORD_CREATION_KEY),
    ENROLMENT_RECORD_DELETION(ApiEventPayloadType.ENROLMENT_RECORD_DELETION_KEY),
    ENROLMENT_RECORD_MOVE(ApiEventPayloadType.ENROLMENT_RECORD_MOVE_KEY),
    CALLOUT(ApiEventPayloadType.CALLOUT_KEY),
    CALLBACK(ApiEventPayloadType.CALLBACK_KEY),
    ARTIFICIAL_TERMINATION(ApiEventPayloadType.ARTIFICIAL_TERMINATION_KEY),
    AUTHENTICATION(ApiEventPayloadType.AUTHENTICATION_KEY),
    CONSENT(ApiEventPayloadType.CONSENT_KEY),
    ENROLMENT(ApiEventPayloadType.ENROLMENT_KEY),
    AUTHORIZATION(ApiEventPayloadType.AUTHORIZATION_KEY),
    FINGERPRINT_CAPTURE(ApiEventPayloadType.FINGERPRINT_CAPTURE_KEY),
    ONE_TO_ONE_MATCH(ApiEventPayloadType.ONE_TO_ONE_MATCH_KEY),
    ONE_TO_MANY_MATCH(ApiEventPayloadType.ONE_TO_MANY_MATCH_KEY),
    PERSON_CREATION(ApiEventPayloadType.PERSON_CREATION_KEY),
    ALERT_SCREEN(ApiEventPayloadType.ALERT_SCREEN_KEY),
    GUID_SELECTION(ApiEventPayloadType.GUID_SELECTION_KEY),
    CONNECTIVITY_SNAPSHOT(ApiEventPayloadType.CONNECTIVITY_SNAPSHOT_KEY),
    REFUSAL(ApiEventPayloadType.REFUSAL_KEY),
    CANDIDATE_READ(ApiEventPayloadType.CANDIDATE_READ_KEY),
    SCANNER_CONNECTION(ApiEventPayloadType.SCANNER_CONNECTION_KEY),
    VERO_2_INFO_SNAPSHOT(ApiEventPayloadType.VERO_2_INFO_SNAPSHOT_KEY),
    SCANNER_FIRMWARE_UPDATE(ApiEventPayloadType.SCANNER_FIRMWARE_UPDATE_KEY),
    INVALID_INTENT(ApiEventPayloadType.INVALID_INTENT_KEY),
    SUSPICIOUS_INTENT(ApiEventPayloadType.SUSPICIOUS_INTENT_KEY),
    INTENT_PARSING(ApiEventPayloadType.INTENT_PARSING_KEY),
    COMPLETION_CHECK(ApiEventPayloadType.COMPLETION_CHECK_KEY),
    SESSION_CAPTURE(ApiEventPayloadType.SESSION_CAPTURE_KEY),
    FACE_ONBOARDING_COMPLETE(ApiEventPayloadType.FACE_ONBOARDING_COMPLETE_KEY),
    FACE_FALLBACK_CAPTURE(ApiEventPayloadType.FACE_FALLBACK_CAPTURE_KEY),
    FACE_CAPTURE(ApiEventPayloadType.FACE_CAPTURE_KEY),
    FACE_CAPTURE_CONFIRMATION(ApiEventPayloadType.FACE_CAPTURE_CONFIRMATION_KEY),
    FACE_CAPTURE_RETRY(ApiEventPayloadType.REFUSAL_KEY);

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
    ENROLMENT_RECORD_CREATION -> ApiEventPayloadType.ENROLMENT_RECORD_CREATION
    ENROLMENT_RECORD_DELETION -> ApiEventPayloadType.ENROLMENT_RECORD_DELETION
    ARTIFICIAL_TERMINATION -> ApiEventPayloadType.ARTIFICIAL_TERMINATION
    AUTHENTICATION -> ApiEventPayloadType.AUTHENTICATION
    CONSENT -> ApiEventPayloadType.CONSENT
    ENROLMENT -> ApiEventPayloadType.ENROLMENT
    AUTHORIZATION -> ApiEventPayloadType.AUTHORIZATION
    FINGERPRINT_CAPTURE -> ApiEventPayloadType.FINGERPRINT_CAPTURE
    ONE_TO_ONE_MATCH -> ApiEventPayloadType.ONE_TO_MANY_MATCH
    ONE_TO_MANY_MATCH -> ApiEventPayloadType.ONE_TO_MANY_MATCH
    PERSON_CREATION -> ApiEventPayloadType.PERSON_CREATION
    ALERT_SCREEN -> ApiEventPayloadType.ALERT_SCREEN
    GUID_SELECTION -> ApiEventPayloadType.GUID_SELECTION
    CONNECTIVITY_SNAPSHOT -> ApiEventPayloadType.CONNECTIVITY_SNAPSHOT
    REFUSAL -> ApiEventPayloadType.REFUSAL
    CANDIDATE_READ -> ApiEventPayloadType.CANDIDATE_READ
    SCANNER_CONNECTION -> ApiEventPayloadType.SCANNER_CONNECTION
    VERO_2_INFO_SNAPSHOT -> ApiEventPayloadType.VERO_2_INFO_SNAPSHOT
    SCANNER_FIRMWARE_UPDATE -> ApiEventPayloadType.SCANNER_FIRMWARE_UPDATE
    INVALID_INTENT -> ApiEventPayloadType.INVALID_INTENT
    CALLOUT_CONFIRMATION,
    CALLOUT_IDENTIFICATION,
    CALLOUT_ENROLMENT,
    CALLOUT_VERIFICATION,
    CALLOUT_LAST_BIOMETRICS -> ApiEventPayloadType.CALLOUT
    CALLBACK_IDENTIFICATION,
    CALLBACK_ENROLMENT,
    CALLBACK_REFUSAL,
    CALLBACK_VERIFICATION,
    CALLBACK_CONFIRMATION,
    CALLBACK_ERROR -> ApiEventPayloadType.CALLBACK
    SUSPICIOUS_INTENT -> ApiEventPayloadType.SUSPICIOUS_INTENT
    INTENT_PARSING -> ApiEventPayloadType.INTENT_PARSING
    COMPLETION_CHECK -> ApiEventPayloadType.COMPLETION_CHECK
    SESSION_CAPTURE -> ApiEventPayloadType.SESSION_CAPTURE
    ENROLMENT_RECORD_MOVE -> ApiEventPayloadType.ENROLMENT_RECORD_MOVE
    FACE_ONBOARDING_COMPLETE -> ApiEventPayloadType.FACE_ONBOARDING_COMPLETE
    FACE_FALLBACK_CAPTURE -> ApiEventPayloadType.FACE_FALLBACK_CAPTURE
    FACE_CAPTURE -> ApiEventPayloadType.FACE_CAPTURE
    FACE_CAPTURE_CONFIRMATION -> ApiEventPayloadType.FACE_CAPTURE_CONFIRMATION
    FACE_CAPTURE_RETRY -> ApiEventPayloadType.FACE_CAPTURE_RETRY
}


fun ApiEventPayloadType.fromApiToDomain(): EventType = when (this) {
    ApiEventPayloadType.ENROLMENT_RECORD_CREATION -> ENROLMENT_RECORD_CREATION
    ApiEventPayloadType.ENROLMENT_RECORD_DELETION -> ENROLMENT_RECORD_DELETION
    ApiEventPayloadType.ARTIFICIAL_TERMINATION -> ARTIFICIAL_TERMINATION
    ApiEventPayloadType.AUTHENTICATION -> AUTHENTICATION
    ApiEventPayloadType.CONSENT -> CONSENT
    ApiEventPayloadType.ENROLMENT -> ENROLMENT
    ApiEventPayloadType.AUTHORIZATION -> AUTHORIZATION
    ApiEventPayloadType.FINGERPRINT_CAPTURE -> FINGERPRINT_CAPTURE
    ApiEventPayloadType.ONE_TO_ONE_MATCH -> ONE_TO_MANY_MATCH
    ApiEventPayloadType.ONE_TO_MANY_MATCH -> ONE_TO_MANY_MATCH
    ApiEventPayloadType.PERSON_CREATION -> PERSON_CREATION
    ApiEventPayloadType.ALERT_SCREEN -> ALERT_SCREEN
    ApiEventPayloadType.GUID_SELECTION -> GUID_SELECTION
    ApiEventPayloadType.CONNECTIVITY_SNAPSHOT -> CONNECTIVITY_SNAPSHOT
    ApiEventPayloadType.REFUSAL -> REFUSAL
    ApiEventPayloadType.CANDIDATE_READ -> CANDIDATE_READ
    ApiEventPayloadType.SCANNER_CONNECTION -> SCANNER_CONNECTION
    ApiEventPayloadType.VERO_2_INFO_SNAPSHOT -> VERO_2_INFO_SNAPSHOT
    ApiEventPayloadType.SCANNER_FIRMWARE_UPDATE -> SCANNER_FIRMWARE_UPDATE
    ApiEventPayloadType.INVALID_INTENT -> INVALID_INTENT
    ApiEventPayloadType.SUSPICIOUS_INTENT -> SUSPICIOUS_INTENT
    ApiEventPayloadType.INTENT_PARSING -> INTENT_PARSING
    ApiEventPayloadType.COMPLETION_CHECK -> COMPLETION_CHECK
    ApiEventPayloadType.SESSION_CAPTURE -> SESSION_CAPTURE
    ApiEventPayloadType.ENROLMENT_RECORD_MOVE -> ENROLMENT_RECORD_MOVE
    ApiEventPayloadType.FACE_ONBOARDING_COMPLETE -> FACE_ONBOARDING_COMPLETE
    ApiEventPayloadType.FACE_FALLBACK_CAPTURE -> FACE_FALLBACK_CAPTURE
    ApiEventPayloadType.FACE_CAPTURE -> FACE_CAPTURE
    ApiEventPayloadType.FACE_CAPTURE_CONFIRMATION -> FACE_CAPTURE_CONFIRMATION
    ApiEventPayloadType.FACE_CAPTURE_RETRY -> FACE_CAPTURE_RETRY
    ApiEventPayloadType.CALLOUT -> throw UnsupportedOperationException("")
    ApiEventPayloadType.CALLBACK -> throw UnsupportedOperationException("")
}
