package com.simprints.id.data.db.event.domain.models.callback

import androidx.annotation.Keep
import com.simprints.id.data.db.event.domain.models.Event
import com.simprints.id.data.db.event.domain.models.EventLabels

import com.simprints.id.data.db.event.domain.models.EventPayload
import com.simprints.id.data.db.event.domain.models.EventType
import com.simprints.id.data.db.event.domain.models.EventType.CALLBACK_REFUSAL
import com.simprints.id.data.db.event.local.models.DbEvent.Companion.DEFAULT_EVENT_VERSION
import java.util.*

@Keep
data class RefusalCallbackEvent(
    override val id: String = UUID.randomUUID().toString(),
    override var labels: EventLabels,
    override val payload: RefusalCallbackPayload,
    override val type: EventType
) : Event() {

    constructor(
        createdAt: Long,
        reason: String,
        extra: String,
        labels: EventLabels = EventLabels()
    ) : this(
        UUID.randomUUID().toString(),
        labels,
        RefusalCallbackPayload(createdAt, EVENT_VERSION, reason, extra),
        CALLBACK_REFUSAL)

    @Keep
    data class RefusalCallbackPayload(
        override val createdAt: Long,
        override val eventVersion: Int,
        val reason: String,
        val extra: String,
        override val type: EventType = CALLBACK_REFUSAL,
        override val endedAt: Long = 0) : EventPayload()

    companion object {
        const val EVENT_VERSION = 1
    }
}
