package com.simprints.id.data.db.person.domain.personevents

import com.simprints.id.data.db.person.remote.models.personevents.*

abstract class EventPayload(val type: EventPayloadType)

fun ApiEventPayload.fromApiToDomain() = when(this.type) {
    ApiEventPayloadType.EnrolmentRecordCreation -> EnrolmentRecordCreationPayload(this as ApiEnrolmentRecordCreationPayload)
    ApiEventPayloadType.EnrolmentRecordDeletion -> EnrolmentRecordDeletionPayload(this as ApiEnrolmentRecordDeletionPayload)
    ApiEventPayloadType.EnrolmentRecordMove -> EnrolmentRecordMovePayload(this as ApiEnrolmentRecordMovePayload)
}
