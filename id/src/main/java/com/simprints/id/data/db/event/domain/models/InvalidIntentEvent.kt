package com.simprints.id.data.db.event.domain.models

import androidx.annotation.Keep
import com.simprints.id.data.db.event.domain.models.EventType.INVALID_INTENT
import java.util.*

@Keep
data class InvalidIntentEvent(
    override val id: String = UUID.randomUUID().toString(),
    override var labels: EventLabels,
    override val payload: InvalidIntentPayload,
    override val type: EventType
) : Event() {

    constructor(
        creationTime: Long,
        action: String,
        extras: Map<String, Any?>,
        labels: EventLabels = EventLabels()
    ) : this(
        UUID.randomUUID().toString(),
        labels,
        InvalidIntentPayload(creationTime, EVENT_VERSION, action, extras),
        INVALID_INTENT)


    @Keep
    data class InvalidIntentPayload(
        override val createdAt: Long,
        override val eventVersion: Int,
        val action: String,
        val extras: Map<String, Any?>,
        override val type: EventType = INVALID_INTENT,
        override val endedAt: Long = 0) : EventPayload()

    companion object {
        const val EVENT_VERSION = 1
    }
}
