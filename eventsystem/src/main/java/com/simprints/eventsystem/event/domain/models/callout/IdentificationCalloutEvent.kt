package com.simprints.eventsystem.event.domain.models.callout

import androidx.annotation.Keep
import com.simprints.eventsystem.event.domain.models.Event
import com.simprints.eventsystem.event.domain.models.EventLabels
import com.simprints.eventsystem.event.domain.models.EventPayload
import com.simprints.eventsystem.event.domain.models.EventType
import com.simprints.eventsystem.event.domain.models.EventType.CALLOUT_IDENTIFICATION
import java.util.UUID

@Keep
data class IdentificationCalloutEvent(
    override val id: String = UUID.randomUUID().toString(),
    override var labels: EventLabels,
    override val payload: IdentificationCalloutPayload,
    override val type: EventType
) : Event() {

    constructor(
        createdAt: Long,
        projectId: String,
        userId: String,
        moduleId: String,
        metadata: String?,
        labels: EventLabels = EventLabels()
    ) : this(
        UUID.randomUUID().toString(),
        labels,
        IdentificationCalloutPayload(createdAt, EVENT_VERSION, projectId, userId, moduleId, metadata),
        CALLOUT_IDENTIFICATION)

    @Keep
    data class IdentificationCalloutPayload(
        override val createdAt: Long,
        override val eventVersion: Int,
        val projectId: String,
        val userId: String,
        val moduleId: String,
        val metadata: String?,
        override val type: EventType = CALLOUT_IDENTIFICATION,
        override val endedAt: Long = 0
    ) : EventPayload()

    companion object {
        const val EVENT_VERSION = 1
    }
}
