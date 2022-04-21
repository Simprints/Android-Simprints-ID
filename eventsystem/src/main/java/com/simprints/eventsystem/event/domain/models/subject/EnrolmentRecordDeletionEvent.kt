package com.simprints.eventsystem.event.domain.models.subject

import androidx.annotation.Keep
import com.simprints.eventsystem.event.domain.models.Event
import com.simprints.eventsystem.event.domain.models.EventLabels
import com.simprints.eventsystem.event.domain.models.EventPayload
import com.simprints.eventsystem.event.domain.models.EventType
import com.simprints.eventsystem.event.domain.models.EventType.ENROLMENT_RECORD_DELETION
import java.util.*

@Keep
data class EnrolmentRecordDeletionEvent(
    override val id: String = UUID.randomUUID().toString(),
    override var labels: EventLabels,
    override val payload: EnrolmentRecordDeletionPayload,
    override val type: EventType
) : Event() {

    constructor(
        createdAt: Long,
        subjectId: String,
        projectId: String,
        moduleId: String,
        attendantId: String,
        extraLabels: EventLabels = EventLabels()
    ) : this(
        UUID.randomUUID().toString(),
        extraLabels.copy(projectId = projectId, moduleIds = listOf(moduleId), attendantId = attendantId),
        EnrolmentRecordDeletionPayload(createdAt, EVENT_VERSION, subjectId, projectId, moduleId, attendantId),
        ENROLMENT_RECORD_DELETION)

    data class EnrolmentRecordDeletionPayload(
        override val createdAt: Long,
        override val eventVersion: Int,
        val subjectId: String,
        val projectId: String,
        val moduleId: String,
        val attendantId: String,
        override val type: EventType = ENROLMENT_RECORD_DELETION,
        override val endedAt: Long = 0
    ) : EventPayload()

    companion object {
        const val EVENT_VERSION = 0
    }
}
