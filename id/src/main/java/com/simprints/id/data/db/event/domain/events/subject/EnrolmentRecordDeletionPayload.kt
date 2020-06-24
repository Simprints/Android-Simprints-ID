package com.simprints.id.data.db.event.domain.events.subject

import com.simprints.id.data.db.event.domain.events.EventPayload
import com.simprints.id.data.db.event.domain.events.EventPayloadType
import com.simprints.id.data.db.event.remote.events.subject.ApiEnrolmentRecordDeletionPayload

data class EnrolmentRecordDeletionPayload(
    val subjectId: String,
    val projectId: String,
    val moduleId: String,
    val attendantId: String
) : EventPayload(EventPayloadType.ENROLMENT_RECORD_DELETION, 0, 0)
// startTime and relativeStartTime are not used for Pokodex events

fun ApiEnrolmentRecordDeletionPayload.fromApiToDomain() =
    EnrolmentRecordDeletionPayload(subjectId, projectId, moduleId, attendantId)
