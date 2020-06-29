package com.simprints.id.data.db.event.remote.events

import androidx.annotation.Keep
import com.simprints.id.data.db.common.models.EventCount
import com.simprints.id.data.db.event.domain.events.EventPayloadType

@Keep
class ApiEventCount(val type: ApiEventPayloadType, val count: Int)

fun ApiEventCount.fromApiToDomain() = when(type) {
    ApiEventPayloadType.ENROLMENT_RECORD_CREATION -> EventCount(type, count)
    ApiEventPayloadType.ENROLMENT_RECORD_DELETION -> EventCount(EventPayloadType.ENROLMENT_RECORD_DELETION, count)
    ApiEventPayloadType.ENROLMENT_RECORD_MOVE -> EventCount(EventPayloadType.ENROLMENT_RECORD_MOVE, count)
    ApiEventPayloadType.CALLOUT -> TODO()
    ApiEventPayloadType.CALLBACK -> TODO()
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
    ApiEventPayloadType.SUSPICIOUS_INTENT -> TODO()
    ApiEventPayloadType.INTENT_PARSING -> TODO()
    ApiEventPayloadType.COMPLETION_CHECK -> TODO()
}
