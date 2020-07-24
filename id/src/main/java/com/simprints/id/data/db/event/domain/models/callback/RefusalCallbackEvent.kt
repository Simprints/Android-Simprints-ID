package com.simprints.id.data.db.event.domain.models.callback

import androidx.annotation.Keep
import com.simprints.id.data.db.event.domain.models.Event
import com.simprints.id.data.db.event.domain.models.EventLabels

import com.simprints.id.data.db.event.domain.models.EventPayload
import com.simprints.id.data.db.event.domain.models.EventType
import com.simprints.id.data.db.event.domain.models.EventType.CALLBACK_REFUSAL
import java.util.*

@Keep
data class RefusalCallbackEvent(
    override val id: String = UUID.randomUUID().toString(),
    override var labels: EventLabels,
    override val payload: RefusalCallbackPayload,
    override val type: EventType
) : Event(id, labels, payload, type) {

    constructor(
        createdAt: Long,
        reason: String,
        extra: String,
        labels: EventLabels = EventLabels() //StopShip: to change in PAS-993
    ) : this(
        UUID.randomUUID().toString(),
        labels,
        RefusalCallbackPayload(createdAt, DEFAULT_EVENT_VERSION, reason, extra),
        CALLBACK_REFUSAL)

    @Keep
    data class RefusalCallbackPayload(
        override val createdAt: Long,
        override val eventVersion: Int,
        val reason: String,
        val extra: String) : EventPayload(CALLBACK_REFUSAL, eventVersion, createdAt)

    companion object {
        const val EVENT_VERSION = DEFAULT_EVENT_VERSION
    }
}
