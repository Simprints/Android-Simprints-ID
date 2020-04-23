package com.simprints.id.data.db.person.remote.models.personevents

import androidx.annotation.Keep
import com.simprints.id.data.db.person.domain.personevents.EventPayloadType

@Keep
enum class ApiEventPayloadType(val apiName: String) {
    ENROLMENT_RECORD_CREATION("EnrolmentRecordCreation"),
    ENROLMENT_RECORD_DELETION("EnrolmentRecordDeletion"),
    ENROLMENT_RECORD_MOVE("EnrolmentRecordMove")
}

fun EventPayloadType.fromDomainToApi() = when(this) {
    EventPayloadType.ENROLMENT_RECORD_CREATION -> ApiEventPayloadType.ENROLMENT_RECORD_CREATION
    EventPayloadType.ENROLMENT_RECORD_DELETION -> ApiEventPayloadType.ENROLMENT_RECORD_DELETION
    EventPayloadType.ENROLMENT_RECORD_MOVE -> ApiEventPayloadType.ENROLMENT_RECORD_MOVE
}
