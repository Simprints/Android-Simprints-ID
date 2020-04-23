package com.simprints.id.data.db.person.domain.personevents

import com.simprints.id.data.db.person.remote.models.personevents.*

abstract class EventPayload(val type: EventPayloadType)

fun ApiEventPayload.fromApiToDomain() = when(this.type) {
    ApiEventPayloadType.ENROLMENT_RECORD_CREATION -> EnrolmentRecordCreationPayload(this as ApiEnrolmentRecordCreationPayload)
    ApiEventPayloadType.ENROLMENT_RECORD_DELETION -> EnrolmentRecordDeletionPayload(this as ApiEnrolmentRecordDeletionPayload)
    ApiEventPayloadType.ENROLMENT_RECORD_MOVE -> EnrolmentRecordMovePayload(this as ApiEnrolmentRecordMovePayload)
}
