package com.simprints.id.data.db.event.domain.models

import androidx.annotation.Keep

import com.simprints.id.data.db.event.domain.models.EventType.COMPLETION_CHECK
import com.simprints.id.data.db.event.local.models.DbEvent.Companion.DEFAULT_EVENT_VERSION
import java.util.*

@Keep
data class CompletionCheckEvent(
    override val id: String = UUID.randomUUID().toString(),
    override var labels: EventLabels,
    override val payload: CompletionCheckPayload,
    override val type: EventType
) : Event() {

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
        val completed: Boolean,
        override val type: EventType = COMPLETION_CHECK,
        override val endedAt: Long = 0) : EventPayload()

    companion object {
        const val EVENT_VERSION = DEFAULT_EVENT_VERSION
    }
}
