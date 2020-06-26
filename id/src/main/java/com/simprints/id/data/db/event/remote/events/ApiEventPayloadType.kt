package com.simprints.id.data.db.event.remote.events

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import com.simprints.id.data.db.event.domain.events.EventPayloadType
import com.simprints.id.data.db.event.remote.events.SerialisedApiNames.ENROLMENT_RECORD_CREATION_SERIALISED
import com.simprints.id.data.db.event.remote.events.SerialisedApiNames.ENROLMENT_RECORD_DELETION_SERIALISED
import com.simprints.id.data.db.event.remote.events.SerialisedApiNames.ENROLMENT_RECORD_MOVE_SERIALISED

@Keep
enum class ApiEventPayloadType(val apiName: String) {
    @SerializedName(ENROLMENT_RECORD_CREATION_SERIALISED) ENROLMENT_RECORD_CREATION(ENROLMENT_RECORD_CREATION_SERIALISED),
    @SerializedName(ENROLMENT_RECORD_DELETION_SERIALISED) ENROLMENT_RECORD_DELETION(ENROLMENT_RECORD_DELETION_SERIALISED),
    @SerializedName(ENROLMENT_RECORD_MOVE_SERIALISED) ENROLMENT_RECORD_MOVE(ENROLMENT_RECORD_MOVE_SERIALISED);
}

private object SerialisedApiNames {
    const val ENROLMENT_RECORD_CREATION_SERIALISED = "EnrolmentRecordCreation"
    const val ENROLMENT_RECORD_DELETION_SERIALISED = "EnrolmentRecordDeletion"
    const val ENROLMENT_RECORD_MOVE_SERIALISED = "EnrolmentRecordMove"
}

fun EventPayloadType.fromDomainToApi() = when(this) {
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
