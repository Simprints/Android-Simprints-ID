package com.simprints.id.data.db.event.domain.models

import androidx.annotation.Keep

import com.simprints.id.data.db.event.domain.models.EventType.COMPLETION_CHECK
import java.util.*

@Keep
data class CompletionCheckEvent(
    override val id: String = UUID.randomUUID().toString(),
    override var labels: EventLabels,
    override val payload: CompletionCheckPayload,
    override val type: EventType
) : Event(id, labels, payload, type) {

    constructor(
        createdAt: Long,
        completed: Boolean,
        labels: EventLabels = EventLabels() //StopShip: to change in PAS-993
    ) : this(
        UUID.randomUUID().toString(),
        labels,
        CompletionCheckPayload(createdAt, EVENT_VERSION, completed),
        COMPLETION_CHECK)

    @Keep
    data class CompletionCheckPayload(
        override val createdAt: Long,
        override val eventVersion: Int,
        val completed: Boolean) : EventPayload(COMPLETION_CHECK, eventVersion, createdAt)

    companion object {
        const val EVENT_VERSION = DEFAULT_EVENT_VERSION
    }
}
