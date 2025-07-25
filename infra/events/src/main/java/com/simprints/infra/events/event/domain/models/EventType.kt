package com.simprints.infra.events.event.domain.models

import androidx.annotation.Keep

@Keep
enum class EventType {
    // a constant key is required to serialise/deserialize
    // events correctly with Jackson (see annotation in Event).
    // Add a key in the companion object for each enum value

    // key added: AUTHENTICATION_KEY
    AUTHENTICATION,

    // key added: CONSENT_KEY
    CONSENT,

    // key added: ENROLMENT_V2_KEY
    ENROLMENT_V2,

    // key added: ENROLMENT_V4_KEY
    ENROLMENT_V4,

    // key added: AUTHORIZATION_KEY
    AUTHORIZATION,

    // key added: FINGERPRINT_CAPTURE_KEY
    FINGERPRINT_CAPTURE,

    // key added: FINGERPRINT_CAPTURE_BIOMETRICS_KEY
    FINGERPRINT_CAPTURE_BIOMETRICS,

    // key added: ONE_TO_ONE_MATCH_KEY
    ONE_TO_ONE_MATCH,

    // key added: ONE_TO_MANY_MATCH_KEY
    ONE_TO_MANY_MATCH,

    // key added: ALERT_SCREEN_KEY
    ALERT_SCREEN,

    // key added: GUID_SELECTION_KEY
    GUID_SELECTION,

    // key added: CONNECTIVITY_SNAPSHOT_KEY
    CONNECTIVITY_SNAPSHOT,

    // key added: REFUSAL_KEY
    REFUSAL,

    // key added: CANDIDATE_READ_KEY
    CANDIDATE_READ,

    // key added: SCANNER_CONNECTION_KEY
    SCANNER_CONNECTION,

    // key added: VERO_2_INFO_SNAPSHOT_KEY
    VERO_2_INFO_SNAPSHOT,

    // key added: SCANNER_FIRMWARE_UPDATE_KEY
    SCANNER_FIRMWARE_UPDATE,

    // key added: INVALID_INTENT_KEY
    INVALID_INTENT,

    // key added: CALLOUT_CONFIRMATION_KEY
    CALLOUT_CONFIRMATION,

    // key added: CALLOUT_CONFIRMATION_V3_KEY
    CALLOUT_CONFIRMATION_V3,

    // key added: CALLOUT_IDENTIFICATION_KEY
    CALLOUT_IDENTIFICATION,

    // key added: CALLOUT_IDENTIFICATION_V3_KEY
    CALLOUT_IDENTIFICATION_V3,

    // key added: CALLOUT_ENROLMENT_KEY
    CALLOUT_ENROLMENT,

    // key added: CALLOUT_ENROLMENT_V3_KEY
    CALLOUT_ENROLMENT_V3,

    // key added: CALLOUT_VERIFICATION_KEY
    CALLOUT_VERIFICATION,

    // key added: CALLOUT_VERIFICATION_V3_KEY
    CALLOUT_VERIFICATION_V3,

    // key added: CALLOUT_LAST_BIOMETRICS_KEY
    CALLOUT_LAST_BIOMETRICS,

    // key added: CALLOUT_LAST_BIOMETRICS_V3_KEY
    CALLOUT_LAST_BIOMETRICS_V3,

    // key added: CALLBACK_IDENTIFICATION_KEY
    CALLBACK_IDENTIFICATION,

    // key added: CALLBACK_ENROLMENT_KEY
    CALLBACK_ENROLMENT,

    // key added: CALLBACK_REFUSAL_KEY
    CALLBACK_REFUSAL,

    // key added: CALLBACK_VERIFICATION_KEY
    CALLBACK_VERIFICATION,

    // key added: CALLBACK_ERROR_KEY
    CALLBACK_ERROR,

    // key added: SUSPICIOUS_INTENT_KEY
    SUSPICIOUS_INTENT,

    // key added: INTENT_PARSING_KEY
    INTENT_PARSING,

    // key added: COMPLETION_CHECK_KEY
    COMPLETION_CHECK,

    // key added: CALLBACK_CONFIRMATION_KEY
    CALLBACK_CONFIRMATION,

    // key added: FACE_ONBOARDING_COMPLETE_KEY
    FACE_ONBOARDING_COMPLETE,

    // key added: FACE_FALLBACK_CAPTURE_KEY
    FACE_FALLBACK_CAPTURE,

    // key added: FACE_CAPTURE_KEY
    FACE_CAPTURE,

    // key added: FACE_CAPTURE_BIOMETRICS_KEY
    FACE_CAPTURE_BIOMETRICS,

    // key added: FACE_CAPTURE_CONFIRMATION_KEY
    FACE_CAPTURE_CONFIRMATION,

    // key added: PERSON_CREATION_KEY
    PERSON_CREATION,

    // key added: EVENT_DOWN_SYNC_REQUEST_KEY
    EVENT_DOWN_SYNC_REQUEST,

    // key added: EVENT_UP_SYNC_REQUEST_KEY
    EVENT_UP_SYNC_REQUEST,

    // key added: SAMPLE_UP_SYNC_REQUEST
    SAMPLE_UP_SYNC_REQUEST,

    // key added: LICENSE_CHECK_KEY
    LICENSE_CHECK,

    // key added: AGE_GROUP_SELECTION_KEY
    AGE_GROUP_SELECTION,

    // key added: BIOMETRIC_REFERENCE_CREATION_KEY
    BIOMETRIC_REFERENCE_CREATION,
    ;

    companion object {
        const val CALLBACK_ENROLMENT_KEY = "CALLBACK_ENROLMENT"
        const val CALLBACK_REFUSAL_KEY = "CALLBACK_REFUSAL"
        const val CALLBACK_VERIFICATION_KEY = "CALLBACK_VERIFICATION"
        const val CALLBACK_ERROR_KEY = "CALLBACK_ERROR"
        const val CALLBACK_CONFIRMATION_KEY = "CALLBACK_CONFIRMATION"
        const val CALLOUT_CONFIRMATION_KEY = "CALLOUT_CONFIRMATION"
        const val CALLOUT_CONFIRMATION_V3_KEY = "CALLOUT_CONFIRMATION_V3"
        const val CALLOUT_IDENTIFICATION_KEY = "CALLOUT_IDENTIFICATION"
        const val CALLOUT_IDENTIFICATION_V3_KEY = "CALLOUT_IDENTIFICATION_V3"
        const val CALLOUT_ENROLMENT_KEY = "CALLOUT_ENROLMENT"
        const val CALLOUT_ENROLMENT_V3_KEY = "CALLOUT_ENROLMENT_V3"
        const val CALLOUT_VERIFICATION_KEY = "CALLOUT_VERIFICATION"
        const val CALLOUT_VERIFICATION_V3_KEY = "CALLOUT_VERIFICATION_V3"
        const val CALLOUT_LAST_BIOMETRICS_KEY = "CALLOUT_LAST_BIOMETRICS"
        const val CALLOUT_LAST_BIOMETRICS_V3_KEY = "CALLOUT_LAST_BIOMETRICS_V3"
        const val CALLBACK_IDENTIFICATION_KEY = "CALLBACK_IDENTIFICATION"
        const val FACE_ONBOARDING_COMPLETE_KEY = "FACE_ONBOARDING_COMPLETE"
        const val FACE_FALLBACK_CAPTURE_KEY = "FACE_FALLBACK_CAPTURE"
        const val FACE_CAPTURE_KEY = "FACE_CAPTURE"
        const val FACE_CAPTURE_BIOMETRICS_KEY = "FACE_CAPTURE_BIOMETRICS"
        const val FACE_CAPTURE_CONFIRMATION_KEY = "FACE_CAPTURE_CONFIRMATION"
        const val AUTHENTICATION_KEY = "AUTHENTICATION"
        const val CONSENT_KEY = "CONSENT"
        const val ENROLMENT_V2_KEY = "ENROLMENT_V2"
        const val ENROLMENT_V4_KEY = "ENROLMENT_V4"
        const val AUTHORIZATION_KEY = "AUTHORIZATION"
        const val FINGERPRINT_CAPTURE_KEY = "FINGERPRINT_CAPTURE"
        const val FINGERPRINT_CAPTURE_BIOMETRICS_KEY = "FINGERPRINT_CAPTURE_BIOMETRICS"
        const val ONE_TO_ONE_MATCH_KEY = "ONE_TO_ONE_MATCH"
        const val ONE_TO_MANY_MATCH_KEY = "ONE_TO_MANY_MATCH"
        const val PERSON_CREATION_KEY = "PERSON_CREATION"
        const val ALERT_SCREEN_KEY = "ALERT_SCREEN"
        const val GUID_SELECTION_KEY = "GUID_SELECTION"
        const val INVALID_INTENT_KEY = "INVALID_INTENT"
        const val INTENT_PARSING_KEY = "INTENT_PARSING"
        const val CONNECTIVITY_SNAPSHOT_KEY = "CONNECTIVITY_SNAPSHOT"
        const val REFUSAL_KEY = "REFUSAL"
        const val CANDIDATE_READ_KEY = "CANDIDATE_READ"
        const val SCANNER_CONNECTION_KEY = "SCANNER_CONNECTION"
        const val SCANNER_FIRMWARE_UPDATE_KEY = "SCANNER_FIRMWARE_UPDATE"
        const val COMPLETION_CHECK_KEY = "COMPLETION_CHECK"
        const val SUSPICIOUS_INTENT_KEY = "SUSPICIOUS_INTENT"
        const val VERO_2_INFO_SNAPSHOT_KEY = "VERO_2_INFO_SNAPSHOT"
        const val EVENT_DOWN_SYNC_REQUEST_KEY = "EVENT_DOWN_SYNC_REQUEST"
        const val EVENT_UP_SYNC_REQUEST_KEY = "EVENT_UP_SYNC_REQUEST"
        const val SAMPLE_UP_SYNC_REQUEST = "SAMPLE_UP_SYNC_REQUEST"
        const val LICENSE_CHECK_KEY = "LICENSE_CHECK"
        const val AGE_GROUP_SELECTION_KEY = "AGE_GROUP_SELECTION"
        const val BIOMETRIC_REFERENCE_CREATION_KEY = "BIOMETRIC_REFERENCE_CREATION"
    }
}
