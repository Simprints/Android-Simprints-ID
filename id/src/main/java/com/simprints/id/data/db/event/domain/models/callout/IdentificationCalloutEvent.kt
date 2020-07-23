package com.simprints.id.data.db.event.domain.models.callout

import androidx.annotation.Keep
import com.simprints.id.data.db.event.domain.models.Event
import com.simprints.id.data.db.event.domain.models.EventLabels

import com.simprints.id.data.db.event.domain.models.EventPayload
import com.simprints.id.data.db.event.domain.models.EventType
import com.simprints.id.data.db.event.domain.models.EventType.CALLOUT_IDENTIFICATION
import java.util.*

@Keep
class IdentificationCalloutEvent(
        override val id: String = UUID.randomUUID().toString(),
        override var labels: EventLabels,
        override val payload: IdentificationCalloutPayload,
        override val type: EventType
) : Event(id, labels, payload, type) {

    constructor(
        createdAt: Long,
        projectId: String,
        userId: String,
        moduleId: String,
        metadata: String?,
        labels: EventLabels = EventLabels() //StopShip: to change in PAS-993
    ) : this(
        UUID.randomUUID().toString(),
        labels,
        IdentificationCalloutPayload(createdAt, EVENT_VERSION, projectId, userId, moduleId, metadata),
        CALLOUT_IDENTIFICATION)

    @Keep
    class IdentificationCalloutPayload(
        createdAt: Long,
        eventVersion: Int,
        val projectId: String,
        val userId: String,
        val moduleId: String,
        val metadata: String?
    ) : EventPayload(CALLOUT_IDENTIFICATION, eventVersion, createdAt)

    companion object {
        const val EVENT_VERSION = DEFAULT_EVENT_VERSION
    }
}
