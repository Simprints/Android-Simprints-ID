package com.simprints.id.data.db.event.domain.models.callout

import androidx.annotation.Keep
import com.simprints.id.data.db.event.domain.models.Event
import com.simprints.id.data.db.event.domain.models.EventLabels

import com.simprints.id.data.db.event.domain.models.EventPayload
import com.simprints.id.data.db.event.domain.models.EventType
import com.simprints.id.data.db.event.domain.models.EventType.CALLOUT_CONFIRMATION
import java.util.*

@Keep
data class ConfirmationCalloutEvent(
    override val id: String = UUID.randomUUID().toString(),
    override var labels: EventLabels,
    override val payload: ConfirmationCalloutPayload,
    override val type: EventType
) : Event(id, labels, payload, type) {

    constructor(
        createdAt: Long,
        projectId: String,
        selectedGuid: String,
        sessionId: String,
        labels: EventLabels = EventLabels() //StopShip: to change in PAS-993
    ) : this(
        UUID.randomUUID().toString(),
        labels,
        ConfirmationCalloutPayload(createdAt, EVENT_VERSION, projectId, selectedGuid, sessionId),
        CALLOUT_CONFIRMATION)

    @Keep
    data class ConfirmationCalloutPayload(
        override val createdAt: Long,
        override val eventVersion: Int,
        val projectId: String,
        val selectedGuid: String,
        val sessionId: String
    ) : EventPayload(CALLOUT_CONFIRMATION, eventVersion, createdAt)

    companion object {
        const val EVENT_VERSION = DEFAULT_EVENT_VERSION
    }
}
