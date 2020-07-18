package com.simprints.id.data.db.event.domain.events.subject

import androidx.annotation.Keep
import com.simprints.id.data.db.event.domain.events.Event
import com.simprints.id.data.db.event.domain.events.EventLabel.*
import com.simprints.id.data.db.event.domain.events.EventPayload
import com.simprints.id.data.db.event.domain.events.EventType
import java.util.*

@Keep
class EnrolmentRecordDeletionEvent(
    createdAt: Long,
    subjectId: String,
    projectId: String,
    moduleId: String,
    attendantId: String
) : Event(
    UUID.randomUUID().toString(),
    mutableListOf(ProjectIdLabel(projectId), ModuleIdsLabel(listOf(moduleId)), AttendantIdLabel(attendantId)),
    EnrolmentRecordDeletionPayload(createdAt, DEFAULT_EVENT_VERSION, subjectId, projectId, moduleId, attendantId)) {

    class EnrolmentRecordDeletionPayload(
        createdAt: Long,
        eventVersion: Int,
        val subjectId: String,
        val projectId: String,
        val moduleId: String,
        val attendantId: String
    ) : EventPayload(EventType.ENROLMENT_RECORD_DELETION, eventVersion, createdAt)
}
