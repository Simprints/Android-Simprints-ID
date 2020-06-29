package com.simprints.id.data.db.event.remote.events

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import com.simprints.id.data.db.event.domain.events.EventPayloadType
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
    @SerializedName(COMPLETION_CHECK_SERIALISED) COMPLETION_CHECK(COMPLETION_CHECK_SERIALISED)
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
}


fun EventPayloadType.fromDomainToApi() = when (this) {
    EventPayloadType.ENROLMENT_RECORD_CREATION -> ApiEventPayloadType.ENROLMENT_RECORD_CREATION
    EventPayloadType.ENROLMENT_RECORD_DELETION -> ApiEventPayloadType.ENROLMENT_RECORD_DELETION
    EventPayloadType.ENROLMENT_RECORD_MOVE -> ApiEventPayloadType.ENROLMENT_RECORD_MOVE
    EventPayloadType.ARTIFICIAL_TERMINATION -> TODO()
    EventPayloadType.AUTHENTICATION -> TODO()
    EventPayloadType.CONSENT -> TODO()
    EventPayloadType.ENROLMENT -> TODO()
    EventPayloadType.AUTHORIZATION -> TODO()
    EventPayloadType.FINGERPRINT_CAPTURE -> TODO()
    EventPayloadType.ONE_TO_ONE_MATCH -> TODO()
    EventPayloadType.ONE_TO_MANY_MATCH -> TODO()
    EventPayloadType.PERSON_CREATION -> TODO()
    EventPayloadType.ALERT_SCREEN -> TODO()
    EventPayloadType.GUID_SELECTION -> TODO()
    EventPayloadType.CONNECTIVITY_SNAPSHOT -> TODO()
    EventPayloadType.REFUSAL -> TODO()
    EventPayloadType.CANDIDATE_READ -> TODO()
    EventPayloadType.SCANNER_CONNECTION -> TODO()
    EventPayloadType.VERO_2_INFO_SNAPSHOT -> TODO()
    EventPayloadType.SCANNER_FIRMWARE_UPDATE -> TODO()
    EventPayloadType.INVALID_INTENT -> TODO()
    EventPayloadType.CALLOUT_CONFIRMATION -> TODO()
    EventPayloadType.CALLOUT_IDENTIFICATION -> TODO()
    EventPayloadType.CALLOUT_ENROLMENT -> TODO()
    EventPayloadType.CALLOUT_VERIFICATION -> TODO()
    EventPayloadType.CALLOUT_LAST_BIOMETRICS -> TODO()
    EventPayloadType.CALLBACK_IDENTIFICATION -> TODO()
    EventPayloadType.CALLBACK_ENROLMENT -> TODO()
    EventPayloadType.CALLBACK_REFUSAL -> TODO()
    EventPayloadType.CALLBACK_VERIFICATION -> TODO()
    EventPayloadType.CALLBACK_ERROR -> TODO()
    EventPayloadType.SUSPICIOUS_INTENT -> TODO()
    EventPayloadType.INTENT_PARSING -> TODO()
    EventPayloadType.COMPLETION_CHECK -> TODO()
    EventPayloadType.CALLBACK_CONFIRMATION -> TODO()
}
