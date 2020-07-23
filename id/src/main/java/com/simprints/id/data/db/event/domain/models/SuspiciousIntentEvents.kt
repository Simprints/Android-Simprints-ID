package com.simprints.id.data.db.event.domain.models

import androidx.annotation.Keep

import com.simprints.id.data.db.event.domain.models.EventType.SUSPICIOUS_INTENT
import java.util.*

@Keep
class SuspiciousIntentEvent(
    override val id: String = UUID.randomUUID().toString(),
    override var labels: EventLabels,
    override val payload: SuspiciousIntentPayload,
    override val type: EventType
) : Event(id, labels, payload, type) {

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
    class SuspiciousIntentPayload(
        createdAt: Long,
        eventVersion: Int,
        val unexpectedExtras: Map<String, Any?>
    ) : EventPayload(SUSPICIOUS_INTENT, eventVersion, createdAt)

    companion object {
        const val EVENT_VERSION = DEFAULT_EVENT_VERSION
    }
}
