package com.simprints.id.data.db.event.domain.events.subject

import androidx.annotation.Keep
import com.simprints.id.data.db.event.domain.events.Event
import com.simprints.id.data.db.event.domain.events.Event.EventLabel.*
import com.simprints.id.data.db.event.domain.events.EventPayload
import com.simprints.id.data.db.event.domain.events.EventPayloadType
import java.util.*

@Keep
class EnrolmentRecordDeletionEvent(
    creationTime: Long,
    subjectId: String,
    projectId: String,
    moduleId: String,
    attendantId: String
) : Event(
    UUID.randomUUID().toString(),
    DEFAULT_EVENT_VERSION,
    listOf(ProjectId(projectId), ModuleId(listOf(moduleId)), AttendantId(attendantId)),
    EnrolmentRecordDeletionPayload(creationTime, DEFAULT_EVENT_VERSION, subjectId, projectId, moduleId, attendantId)) {

    class EnrolmentRecordDeletionPayload(
        creationTime: Long,
        version: Int,
        val subjectId: String,
        val projectId: String,
        val moduleId: String,
        val attendantId: String
    ) : EventPayload(EventPayloadType.ENROLMENT_RECORD_DELETION, version, creationTime)
}
