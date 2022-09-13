package com.simprints.eventsystem.event.domain.models.callout

import androidx.annotation.Keep
import com.simprints.eventsystem.event.domain.models.Event
import com.simprints.eventsystem.event.domain.models.EventLabels
import com.simprints.eventsystem.event.domain.models.EventPayload
import com.simprints.eventsystem.event.domain.models.EventType
import com.simprints.eventsystem.event.domain.models.EventType.CALLOUT_CONFIRMATION
import java.util.*

@Keep
data class ConfirmationCalloutEvent(
    override val id: String = UUID.randomUUID().toString(),
    override var labels: EventLabels,
    override val payload: ConfirmationCalloutPayload,
    override val type: EventType
) : Event() {

    constructor(
        createdAt: Long,
        projectId: String,
        selectedGuid: String,
        sessionId: String,
        labels: EventLabels = EventLabels()
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
        val sessionId: String,
        override val type: EventType = CALLOUT_CONFIRMATION,
        override val endedAt: Long = 0
    ) : EventPayload()

    companion object {
        const val EVENT_VERSION = 1
    }
}
