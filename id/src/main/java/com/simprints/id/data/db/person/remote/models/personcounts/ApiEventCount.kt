package com.simprints.id.data.db.person.remote.models.personcounts

import androidx.annotation.Keep

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
