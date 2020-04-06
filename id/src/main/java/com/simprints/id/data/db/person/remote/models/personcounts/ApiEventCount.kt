package com.simprints.id.data.db.person.remote.models.personcounts

import androidx.annotation.Keep
import com.simprints.id.data.db.common.models.EventCount
import com.simprints.id.data.db.common.models.EventType

@Keep
class ApiEventCounts(val eventCounts: ApiEventCount)

@Keep
class ApiEventCount(val type: ApiEventCountType, val count: Int)

@Keep
enum class ApiEventCountType {
    EnrolmentRecordCreation,
    EnrolmentRecordDeletion,
    EnrolmentRecordMove
}

fun ApiEventCount.fromApiToDomain() = when(type) {
    ApiEventCountType.EnrolmentRecordCreation -> EventCount(EventType.EnrolmentRecordCreation, count)
    ApiEventCountType.EnrolmentRecordDeletion -> EventCount(EventType.EnrolmentRecordDeletion, count)
    ApiEventCountType.EnrolmentRecordMove -> EventCount(EventType.EnrolmentRecordMove, count)
}
