package com.simprints.id.data.db.event.domain.models

import androidx.annotation.Keep
import com.simprints.id.data.db.event.domain.models.EventType.SUSPICIOUS_INTENT
import com.simprints.id.data.db.event.local.models.DbEvent.Companion.DEFAULT_EVENT_VERSION
import java.util.*

@Keep
data class SuspiciousIntentEvent(
    override val id: String = UUID.randomUUID().toString(),
    override var labels: EventLabels,
    override val payload: SuspiciousIntentPayload,
    override val type: EventType
) : Event() {

    constructor(
        createdAt: Long,
        unexpectedExtras: Map<String, Any?>,
        labels: EventLabels = EventLabels()//StopShip: to change in PAS-993
    ) : this(
        UUID.randomUUID().toString(),
        labels,
        SuspiciousIntentPayload(createdAt, EVENT_VERSION, unexpectedExtras),
        SUSPICIOUS_INTENT)

    @Keep
    data class SuspiciousIntentPayload(
        override val createdAt: Long,
        override val eventVersion: Int,
        val unexpectedExtras: Map<String, Any?>,
        override val type: EventType = SUSPICIOUS_INTENT,
        override val endedAt: Long = 0
    ) : EventPayload()

    companion object {
        const val EVENT_VERSION = 1
    }
}
