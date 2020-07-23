package com.simprints.id.data.db.event.domain.models.subject

import androidx.annotation.Keep
import com.simprints.id.data.db.event.domain.models.Event
import com.simprints.id.data.db.event.domain.models.EventLabel
import com.simprints.id.data.db.event.domain.models.EventLabel.*
import com.simprints.id.data.db.event.domain.models.EventPayload
import com.simprints.id.data.db.event.domain.models.EventType
import com.simprints.id.data.db.event.domain.models.EventType.ENROLMENT_RECORD_DELETION
import java.util.*

@Keep
class EnrolmentRecordDeletionEvent(
    override val id: String = UUID.randomUUID().toString(),
    override val labels: MutableList<EventLabel>,
    override val payload: EnrolmentRecordDeletionPayload,
    override val type: EventType
) : Event(id, labels, payload, type) {

    constructor(
        createdAt: Long,
        subjectId: String,
        projectId: String,
        moduleId: String,
        attendantId: String
    ) : this(
        UUID.randomUUID().toString(),
        mutableListOf(ProjectIdLabel(projectId), ModuleIdsLabel(listOf(moduleId)), AttendantIdLabel(attendantId)),
        EnrolmentRecordDeletionPayload(createdAt, EVENT_VERSION, subjectId, projectId, moduleId, attendantId),
        ENROLMENT_RECORD_DELETION)

    class EnrolmentRecordDeletionPayload(
        createdAt: Long,
        eventVersion: Int,
        val subjectId: String,
        val projectId: String,
        val moduleId: String,
        val attendantId: String
    ) : EventPayload(ENROLMENT_RECORD_DELETION, eventVersion, createdAt)

    companion object {
        const val EVENT_VERSION = DEFAULT_EVENT_VERSION
    }
}
