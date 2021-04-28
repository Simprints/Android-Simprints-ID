package com.simprints.id.data.db.event.domain.models

import androidx.annotation.Keep
import com.simprints.id.data.db.event.domain.models.EventType.ENROLMENT_V2
import java.util.*

@Keep
data class EnrolmentEventV2(
    override val id: String = UUID.randomUUID().toString(),
    override var labels: EventLabels,
    override val payload: EnrolmentPayload,
    override val type: EventType
) : Event() {

    constructor(
        createdAt: Long,
        subjectId: String,
        projectId: String,
        moduleId: String,
        attendantId: String,
        personCreationEventId: String,
        labels: EventLabels = EventLabels()
    ) : this(
        UUID.randomUUID().toString(),
        labels,
        EnrolmentPayload(createdAt, EVENT_VERSION, subjectId, projectId, moduleId, attendantId, personCreationEventId),
        ENROLMENT_V2)


    @Keep
    data class EnrolmentPayload(
        override val createdAt: Long,
        override val eventVersion: Int,
        val subjectId: String,
        val projectId: String,
        val moduleId: String,
        val attendantId: String,
        val personCreationEventId: String,
        override val type: EventType = ENROLMENT_V2,
        override val endedAt: Long = 0) : EventPayload()

    companion object {
        const val EVENT_VERSION = 2
    }
}
