package com.simprints.id.data.db.person.remote.models.personcounts

import androidx.annotation.Keep
import com.simprints.id.data.db.common.models.EventCount
import com.simprints.id.data.db.person.domain.personevents.EventPayloadType
import com.simprints.id.data.db.person.remote.models.personevents.ApiEventPayloadType

@Keep
class ApiEventCount(val type: ApiEventPayloadType, val count: Int)

fun ApiEventCount.fromApiToDomain() = when(type) {
    ApiEventPayloadType.ENROLMENT_RECORD_CREATION -> EventCount(EventPayloadType.ENROLMENT_RECORD_CREATION, count)
    ApiEventPayloadType.ENROLMENT_RECORD_DELETION -> EventCount(EventPayloadType.ENROLMENT_RECORD_DELETION, count)
    ApiEventPayloadType.ENROLMENT_RECORD_MOVE -> EventCount(EventPayloadType.ENROLMENT_RECORD_MOVE, count)
}
