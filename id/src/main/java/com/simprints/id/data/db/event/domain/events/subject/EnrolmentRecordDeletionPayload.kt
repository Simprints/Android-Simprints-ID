package com.simprints.id.data.db.event.domain.events.subject

import androidx.annotation.Keep
import com.simprints.id.data.db.event.domain.events.Event
import com.simprints.id.data.db.event.domain.events.EventLabel.*
import com.simprints.id.data.db.event.domain.events.EventPayload
import com.simprints.id.data.db.event.domain.events.EventPayloadType
import java.util.*

@Keep
class EnrolmentRecordDeletionEvent(
    subjectId: String,
    projectId: String,
    moduleId: String,
    attendantId: String
) : Event(
    UUID.randomUUID().toString(),
    listOf(ProjectId(projectId), ModuleId(listOf(moduleId)), AttendantId(attendantId)),
    EnrolmentRecordDeletionPayload(subjectId, projectId, moduleId, attendantId)) {

    data class EnrolmentRecordDeletionPayload(
        val subjectId: String,
        val projectId: String,
        val moduleId: String,
        val attendantId: String
    ) : EventPayload(EventPayloadType.ENROLMENT_RECORD_DELETION, 0)
}
