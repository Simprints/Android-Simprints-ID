package com.simprints.id.data.db.person.domain.personevents

import com.simprints.id.data.db.person.remote.models.personevents.ApiEnrolmentRecordCreationPayload
import com.simprints.id.data.db.person.remote.models.personevents.ApiEnrolmentRecordDeletionPayload
import com.simprints.id.data.db.person.remote.models.personevents.ApiEnrolmentRecordMovePayload
import com.simprints.id.data.db.person.remote.models.personevents.ApiEventPayload

abstract class EventPayload(val type: EventPayloadType)

fun ApiEventPayload.fromApiToDomain() = when(this) {
    is ApiEnrolmentRecordCreationPayload -> EnrolmentRecordCreationPayload(this)
    is ApiEnrolmentRecordDeletionPayload -> EnrolmentRecordDeletionPayload(this)
    is ApiEnrolmentRecordMovePayload -> EnrolmentRecordMovePayload(this)
    else -> throw IllegalStateException("Invalid payload type for events")
}
