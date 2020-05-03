package com.simprints.id.data.db.subject.domain.subjectevents

import com.simprints.id.data.db.subject.remote.models.subjectevents.ApiEnrolmentRecordCreationPayload
import com.simprints.id.data.db.subject.remote.models.subjectevents.ApiEnrolmentRecordDeletionPayload
import com.simprints.id.data.db.subject.remote.models.subjectevents.ApiEnrolmentRecordMovePayload
import com.simprints.id.data.db.subject.remote.models.subjectevents.ApiEventPayload

abstract class EventPayload(val type: EventPayloadType)

fun ApiEventPayload.fromApiToDomain() = when(this) {
    is ApiEnrolmentRecordCreationPayload -> EnrolmentRecordCreationPayload(this)
    is ApiEnrolmentRecordDeletionPayload -> EnrolmentRecordDeletionPayload(this)
    is ApiEnrolmentRecordMovePayload -> EnrolmentRecordMovePayload(this)
    else -> throw IllegalStateException("Invalid payload type for events")
}
