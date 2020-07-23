package com.simprints.id.data.db.event.remote.events

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import com.simprints.id.data.db.event.domain.models.EventType
import com.simprints.id.data.db.event.domain.models.EventType.*
import com.simprints.id.data.db.event.remote.events.SerialisedApiNames.ALERT_SCREEN_SERIALISED
import com.simprints.id.data.db.event.remote.events.SerialisedApiNames.ARTIFICIAL_TERMINATION_SERIALISED
import com.simprints.id.data.db.event.remote.events.SerialisedApiNames.AUTHENTICATION_SERIALISED
import com.simprints.id.data.db.event.remote.events.SerialisedApiNames.AUTHORIZATION_SERIALISED
import com.simprints.id.data.db.event.remote.events.SerialisedApiNames.CALLBACK_SERIALISED
import com.simprints.id.data.db.event.remote.events.SerialisedApiNames.CALLOUT_SERIALISED
import com.simprints.id.data.db.event.remote.events.SerialisedApiNames.CANDIDATE_READ_SERIALISED
import com.simprints.id.data.db.event.remote.events.SerialisedApiNames.COMPLETION_CHECK_SERIALISED
import com.simprints.id.data.db.event.remote.events.SerialisedApiNames.CONNECTIVITY_SNAPSHOT_SERIALISED
import com.simprints.id.data.db.event.remote.events.SerialisedApiNames.CONSENT_SERIALISED
import com.simprints.id.data.db.event.remote.events.SerialisedApiNames.ENROLMENT_RECORD_CREATION_SERIALISED
import com.simprints.id.data.db.event.remote.events.SerialisedApiNames.ENROLMENT_RECORD_DELETION_SERIALISED
import com.simprints.id.data.db.event.remote.events.SerialisedApiNames.ENROLMENT_RECORD_MOVE_SERIALISED
import com.simprints.id.data.db.event.remote.events.SerialisedApiNames.ENROLMENT_SERIALISED
import com.simprints.id.data.db.event.remote.events.SerialisedApiNames.FACE_CAPTURE_CONFIRMATION_SERIALISED
import com.simprints.id.data.db.event.remote.events.SerialisedApiNames.FACE_CAPTURE_RETRY_SERIALISED
import com.simprints.id.data.db.event.remote.events.SerialisedApiNames.FACE_CAPTURE_SERIALISED
import com.simprints.id.data.db.event.remote.events.SerialisedApiNames.FACE_FALLBACK_CAPTURE_SERIALISED
import com.simprints.id.data.db.event.remote.events.SerialisedApiNames.FACE_ONBOARDING_COMPLETE_SERIALISED
import com.simprints.id.data.db.event.remote.events.SerialisedApiNames.FINGERPRINT_CAPTURE_SERIALISED
import com.simprints.id.data.db.event.remote.events.SerialisedApiNames.GUID_SELECTION_SERIALISED
import com.simprints.id.data.db.event.remote.events.SerialisedApiNames.INTENT_PARSING_SERIALISED
import com.simprints.id.data.db.event.remote.events.SerialisedApiNames.INVALID_INTENT_SERIALISED
import com.simprints.id.data.db.event.remote.events.SerialisedApiNames.ONE_TO_MANY_MATCH_SERIALISED
import com.simprints.id.data.db.event.remote.events.SerialisedApiNames.ONE_TO_ONE_MATCH_SERIALISED
import com.simprints.id.data.db.event.remote.events.SerialisedApiNames.PERSON_CREATION_SERIALISED
import com.simprints.id.data.db.event.remote.events.SerialisedApiNames.REFUSAL_SERIALISED
import com.simprints.id.data.db.event.remote.events.SerialisedApiNames.SCANNER_CONNECTION_SERIALISED
import com.simprints.id.data.db.event.remote.events.SerialisedApiNames.SCANNER_FIRMWARE_UPDATE_SERIALISED
import com.simprints.id.data.db.event.remote.events.SerialisedApiNames.SESSION_CAPTURE_SERIALISED
import com.simprints.id.data.db.event.remote.events.SerialisedApiNames.SUSPICIOUS_INTENT_SERIALISED
import com.simprints.id.data.db.event.remote.events.SerialisedApiNames.VERO_2_INFO_SNAPSHOT_SERIALISED

@Keep
enum class ApiEventPayloadType(val apiName: String) {
    @SerializedName(ENROLMENT_RECORD_CREATION_SERIALISED) ENROLMENT_RECORD_CREATION(ENROLMENT_RECORD_CREATION_SERIALISED),
    @SerializedName(ENROLMENT_RECORD_DELETION_SERIALISED) ENROLMENT_RECORD_DELETION(ENROLMENT_RECORD_DELETION_SERIALISED),
    @SerializedName(ENROLMENT_RECORD_MOVE_SERIALISED) ENROLMENT_RECORD_MOVE(ENROLMENT_RECORD_MOVE_SERIALISED),
    @SerializedName(CALLOUT_SERIALISED) CALLOUT(CALLOUT_SERIALISED),
    @SerializedName(CALLBACK_SERIALISED) CALLBACK(CALLBACK_SERIALISED),
    @SerializedName(ARTIFICIAL_TERMINATION_SERIALISED) ARTIFICIAL_TERMINATION(ARTIFICIAL_TERMINATION_SERIALISED),
    @SerializedName(AUTHENTICATION_SERIALISED) AUTHENTICATION(AUTHENTICATION_SERIALISED),
    @SerializedName(CONSENT_SERIALISED) CONSENT(CONSENT_SERIALISED),
    @SerializedName(ENROLMENT_SERIALISED) ENROLMENT(ENROLMENT_SERIALISED),
    @SerializedName(AUTHORIZATION_SERIALISED) AUTHORIZATION(AUTHORIZATION_SERIALISED),
    @SerializedName(FINGERPRINT_CAPTURE_SERIALISED) FINGERPRINT_CAPTURE(FINGERPRINT_CAPTURE_SERIALISED),
    @SerializedName(ONE_TO_ONE_MATCH_SERIALISED) ONE_TO_ONE_MATCH(ONE_TO_ONE_MATCH_SERIALISED),
    @SerializedName(ONE_TO_MANY_MATCH_SERIALISED) ONE_TO_MANY_MATCH(ONE_TO_MANY_MATCH_SERIALISED),
    @SerializedName(PERSON_CREATION_SERIALISED) PERSON_CREATION(PERSON_CREATION_SERIALISED),
    @SerializedName(ALERT_SCREEN_SERIALISED) ALERT_SCREEN(ALERT_SCREEN_SERIALISED),
    @SerializedName(GUID_SELECTION_SERIALISED) GUID_SELECTION(GUID_SELECTION_SERIALISED),
    @SerializedName(CONNECTIVITY_SNAPSHOT_SERIALISED) CONNECTIVITY_SNAPSHOT(CONNECTIVITY_SNAPSHOT_SERIALISED),
    @SerializedName(REFUSAL_SERIALISED) REFUSAL(REFUSAL_SERIALISED),
    @SerializedName(CANDIDATE_READ_SERIALISED) CANDIDATE_READ(CANDIDATE_READ_SERIALISED),
    @SerializedName(SCANNER_CONNECTION_SERIALISED) SCANNER_CONNECTION(SCANNER_CONNECTION_SERIALISED),
    @SerializedName(VERO_2_INFO_SNAPSHOT_SERIALISED) VERO_2_INFO_SNAPSHOT(VERO_2_INFO_SNAPSHOT_SERIALISED),
    @SerializedName(SCANNER_FIRMWARE_UPDATE_SERIALISED) SCANNER_FIRMWARE_UPDATE(SCANNER_FIRMWARE_UPDATE_SERIALISED),
    @SerializedName(INVALID_INTENT_SERIALISED) INVALID_INTENT(INVALID_INTENT_SERIALISED),
    @SerializedName(SUSPICIOUS_INTENT_SERIALISED) SUSPICIOUS_INTENT(SUSPICIOUS_INTENT_SERIALISED),
    @SerializedName(INTENT_PARSING_SERIALISED) INTENT_PARSING(INTENT_PARSING_SERIALISED),
    @SerializedName(COMPLETION_CHECK_SERIALISED) COMPLETION_CHECK(COMPLETION_CHECK_SERIALISED),
    @SerializedName(SESSION_CAPTURE_SERIALISED) SESSION_CAPTURE(SESSION_CAPTURE_SERIALISED),
    @SerializedName(FACE_ONBOARDING_COMPLETE_SERIALISED) FACE_ONBOARDING_COMPLETE(FACE_ONBOARDING_COMPLETE_SERIALISED),
    @SerializedName(FACE_FALLBACK_CAPTURE_SERIALISED) FACE_FALLBACK_CAPTURE(FACE_FALLBACK_CAPTURE_SERIALISED),
    @SerializedName(FACE_CAPTURE_SERIALISED) FACE_CAPTURE(FACE_CAPTURE_SERIALISED),
    @SerializedName(FACE_CAPTURE_CONFIRMATION_SERIALISED) FACE_CAPTURE_CONFIRMATION(FACE_CAPTURE_CONFIRMATION_SERIALISED),
    @SerializedName(FACE_CAPTURE_RETRY_SERIALISED) FACE_CAPTURE_RETRY(FACE_CAPTURE_RETRY_SERIALISED)

}

private object SerialisedApiNames {
    const val ENROLMENT_RECORD_CREATION_SERIALISED = "EnrolmentRecordCreation"
    const val ENROLMENT_RECORD_DELETION_SERIALISED = "EnrolmentRecordDeletion"
    const val ENROLMENT_RECORD_MOVE_SERIALISED = "EnrolmentRecordMove"
    const val CALLOUT_SERIALISED = "Callout"
    const val CALLBACK_SERIALISED = "Callback"
    const val ARTIFICIAL_TERMINATION_SERIALISED = "ArtificialTermination"
    const val AUTHENTICATION_SERIALISED = "Authentication"
    const val CONSENT_SERIALISED = "Consent"
    const val ENROLMENT_SERIALISED = "Enrolment"
    const val AUTHORIZATION_SERIALISED = "Authorization"
    const val FINGERPRINT_CAPTURE_SERIALISED = "FingerprintCapture"
    const val ONE_TO_ONE_MATCH_SERIALISED = "OneToOneMatch"
    const val ONE_TO_MANY_MATCH_SERIALISED = "OneToManyMatch"
    const val PERSON_CREATION_SERIALISED = "PersonCreation"
    const val ALERT_SCREEN_SERIALISED = "AlertScreen"
    const val GUID_SELECTION_SERIALISED = "GuidSelection"
    const val CONNECTIVITY_SNAPSHOT_SERIALISED = "ConnectivitySnapshot"
    const val REFUSAL_SERIALISED = "RefusalSerialised"
    const val CANDIDATE_READ_SERIALISED = "CandidateRead"
    const val SCANNER_CONNECTION_SERIALISED = "ScannerConnection"
    const val VERO_2_INFO_SNAPSHOT_SERIALISED = "Vero2InfoSnapshot"
    const val SCANNER_FIRMWARE_UPDATE_SERIALISED = "ScannerFirmwareUpdate"
    const val INVALID_INTENT_SERIALISED = "InvalidIntent"
    const val SUSPICIOUS_INTENT_SERIALISED = "SuspiciousIntent"
    const val INTENT_PARSING_SERIALISED = "IntentParsing"
    const val COMPLETION_CHECK_SERIALISED = "CompletionCheck"
    const val SESSION_CAPTURE_SERIALISED = "SessionCapture"
    const val FACE_ONBOARDING_COMPLETE_SERIALISED = "FaceOnboardingComplete"
    const val FACE_FALLBACK_CAPTURE_SERIALISED = "FaceFallbackCapture"
    const val FACE_CAPTURE_SERIALISED = "FaceCapture"
    const val FACE_CAPTURE_CONFIRMATION_SERIALISED = "FaceCaptureConfirmation"
    const val FACE_CAPTURE_RETRY_SERIALISED = "FaceCaptureRetry"
}


fun EventType.fromDomainToApi() = when (this) {
    ENROLMENT_RECORD_CREATION -> ApiEventPayloadType.ENROLMENT_RECORD_CREATION
    ENROLMENT_RECORD_DELETION -> ApiEventPayloadType.ENROLMENT_RECORD_DELETION
    ARTIFICIAL_TERMINATION -> TODO()
    AUTHENTICATION -> TODO()
    CONSENT -> TODO()
    ENROLMENT -> TODO()
    AUTHORIZATION -> TODO()
    FINGERPRINT_CAPTURE -> TODO()
    ONE_TO_ONE_MATCH -> TODO()
    ONE_TO_MANY_MATCH -> TODO()
    PERSON_CREATION -> TODO()
    ALERT_SCREEN -> TODO()
    GUID_SELECTION -> TODO()
    CONNECTIVITY_SNAPSHOT -> TODO()
    REFUSAL -> TODO()
    CANDIDATE_READ -> TODO()
    SCANNER_CONNECTION -> TODO()
    VERO_2_INFO_SNAPSHOT -> TODO()
    SCANNER_FIRMWARE_UPDATE -> TODO()
    INVALID_INTENT -> TODO()
    CALLOUT_CONFIRMATION -> TODO()
    CALLOUT_IDENTIFICATION -> TODO()
    CALLOUT_ENROLMENT -> TODO()
    CALLOUT_VERIFICATION -> TODO()
    CALLOUT_LAST_BIOMETRICS -> TODO()
    CALLBACK_IDENTIFICATION -> TODO()
    CALLBACK_ENROLMENT -> TODO()
    CALLBACK_REFUSAL -> TODO()
    CALLBACK_VERIFICATION -> TODO()
    CALLBACK_ERROR -> TODO()
    SUSPICIOUS_INTENT -> TODO()
    INTENT_PARSING -> TODO()
    COMPLETION_CHECK -> TODO()
    CALLBACK_CONFIRMATION -> TODO()
    SESSION_CAPTURE -> TODO()
    ENROLMENT_RECORD_MOVE -> TODO()
    FACE_ONBOARDING_COMPLETE -> TODO()
    FACE_FALLBACK_CAPTURE -> TODO()
    FACE_CAPTURE -> TODO()
    FACE_CAPTURE_CONFIRMATION -> TODO()
    FACE_CAPTURE_RETRY -> TODO()
}

fun ApiEventPayloadType.fromApiToDomain() =
    when (this) {
        ApiEventPayloadType.ENROLMENT_RECORD_CREATION -> ENROLMENT_RECORD_CREATION
        ApiEventPayloadType.ENROLMENT_RECORD_DELETION -> ENROLMENT_RECORD_DELETION
        ApiEventPayloadType.ENROLMENT_RECORD_MOVE -> ENROLMENT_RECORD_MOVE
        ApiEventPayloadType.ARTIFICIAL_TERMINATION -> TODO()
        ApiEventPayloadType.AUTHENTICATION -> TODO()
        ApiEventPayloadType.CONSENT -> TODO()
        ApiEventPayloadType.ENROLMENT -> TODO()
        ApiEventPayloadType.AUTHORIZATION -> TODO()
        ApiEventPayloadType.FINGERPRINT_CAPTURE -> TODO()
        ApiEventPayloadType.ONE_TO_ONE_MATCH -> TODO()
        ApiEventPayloadType.ONE_TO_MANY_MATCH -> TODO()
        ApiEventPayloadType.PERSON_CREATION -> TODO()
        ApiEventPayloadType.ALERT_SCREEN -> TODO()
        ApiEventPayloadType.GUID_SELECTION -> TODO()
        ApiEventPayloadType.CONNECTIVITY_SNAPSHOT -> TODO()
        ApiEventPayloadType.REFUSAL -> TODO()
        ApiEventPayloadType.CANDIDATE_READ -> TODO()
        ApiEventPayloadType.SCANNER_CONNECTION -> TODO()
        ApiEventPayloadType.VERO_2_INFO_SNAPSHOT -> TODO()
        ApiEventPayloadType.SCANNER_FIRMWARE_UPDATE -> TODO()
        ApiEventPayloadType.INVALID_INTENT -> TODO()
        ApiEventPayloadType.CALLBACK -> TODO()
        ApiEventPayloadType.CALLOUT -> TODO()
        ApiEventPayloadType.SUSPICIOUS_INTENT -> TODO()
        ApiEventPayloadType.INTENT_PARSING -> TODO()
        ApiEventPayloadType.COMPLETION_CHECK -> TODO()
        ApiEventPayloadType.SESSION_CAPTURE -> TODO()
        ApiEventPayloadType.FACE_ONBOARDING_COMPLETE -> TODO()
        ApiEventPayloadType.FACE_FALLBACK_CAPTURE -> TODO()
        ApiEventPayloadType.FACE_CAPTURE -> TODO()
        ApiEventPayloadType.FACE_CAPTURE_CONFIRMATION -> TODO()
        ApiEventPayloadType.FACE_CAPTURE_RETRY -> TODO()
    }
