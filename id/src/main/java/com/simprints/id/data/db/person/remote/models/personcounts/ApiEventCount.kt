package com.simprints.id.data.db.person.remote.models.personcounts

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import com.simprints.id.data.db.common.models.EventCount
import com.simprints.id.data.db.common.models.EventType

@Keep
class ApiEventCount(val type: ApiEventCountType, val count: Int)

@Keep
enum class ApiEventCountType {
    @SerializedName("EnrolmentRecordCreation") ENROLMENT_RECORD_CREATION,
    @SerializedName("EnrolmentRecordDeletion") ENROLMENT_RECORD_DELETION,
    @SerializedName("EnrolmentRecordMove") ENROLMENT_RECORD_MOVE
}

fun ApiEventCount.fromApiToDomain() = when(type) {
    ApiEventCountType.ENROLMENT_RECORD_CREATION -> EventCount(EventType.ENROLMENT_RECORD_CREATION, count)
    ApiEventCountType.ENROLMENT_RECORD_DELETION -> EventCount(EventType.ENROLMENT_RECORD_DELETION, count)
    ApiEventCountType.ENROLMENT_RECORD_MOVE -> EventCount(EventType.ENROLMENT_RECORD_MOVE, count)
}
