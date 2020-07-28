package com.simprints.id.data.db.event.remote.models

import com.simprints.id.data.db.event.domain.models.EventType
import com.simprints.id.data.db.event.domain.models.EventType.*

enum class ApiEventPayloadType(val key: String) {
    ENROLMENT_RECORD_CREATION("EnrolmentRecordCreation"),
    ENROLMENT_RECORD_DELETION("EnrolmentRecordDeletion"),
    ENROLMENT_RECORD_MOVE("EnrolmentRecordMove"),
    CALLOUT("Callout"),
    CALLBACK("Callback"),
    ARTIFICIAL_TERMINATION("ArtificialTermination"),
    AUTHENTICATION("Authentication"),
    CONSENT("Consent"),
    ENROLMENT("Enrolment"),
    AUTHORIZATION("Authorization"),
    FINGERPRINT_CAPTURE("FingerprintCapture"),
    ONE_TO_ONE_MATCH("OneToOneMatch"),
    ONE_TO_MANY_MATCH("OneToManyMatch"),
    PERSON_CREATION("PersonCreation"),
    ALERT_SCREEN("AlertScreen"),
    GUID_SELECTION("GuidSelection"),
    CONNECTIVITY_SNAPSHOT("ConnectivitySnapshot"),
    REFUSAL("RefusalSerialised"),
    CANDIDATE_READ("CandidateRead"),
    SCANNER_CONNECTION("ScannerConnection"),
    VERO_2_INFO_SNAPSHOT("Vero2InfoSnapshot"),
    SCANNER_FIRMWARE_UPDATE("ScannerFirmwareUpdate"),
    INVALID_INTENT("InvalidIntent"),
    SUSPICIOUS_INTENT("SuspiciousIntent"),
    INTENT_PARSING("IntentParsing"),
    COMPLETION_CHECK("CompletionCheck"),
    SESSION_CAPTURE("SessionCapture"),
    FACE_ONBOARDING_COMPLETE("FaceOnboardingComplete"),
    FACE_FALLBACK_CAPTURE("FaceFallbackCapture"),
    FACE_CAPTURE("FaceCapture"),
    FACE_CAPTURE_CONFIRMATION("FaceCaptureConfirmation"),
    FACE_CAPTURE_RETRY("FaceCaptureRetry"),
}


fun EventType.fromDomainToApi() = when (this) {
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
