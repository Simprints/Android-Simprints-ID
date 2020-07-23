package com.simprints.id.data.db.event.domain.models

import androidx.annotation.Keep

import com.simprints.id.data.db.event.domain.models.EventType.ARTIFICIAL_TERMINATION
import java.util.*

@Keep
class ArtificialTerminationEvent(
    override val id: String = UUID.randomUUID().toString(),
    override var labels: EventLabels,
    override val payload: ArtificialTerminationPayload,
    override val type: EventType
) : Event(id, labels, payload, type) {

    constructor(
        createdAt: Long,
        reason: ArtificialTerminationPayload.Reason,
        labels: EventLabels = EventLabels()//StopShip: to change in PAS-993
    ) : this(
        UUID.randomUUID().toString(),
        labels,
        ArtificialTerminationPayload(createdAt, EVENT_VERSION, reason),
        ARTIFICIAL_TERMINATION)


    @Keep
    class ArtificialTerminationPayload(
        createdAt: Long,
        eventVersion: Int,
        val reason: Reason
    ) : EventPayload(ARTIFICIAL_TERMINATION, eventVersion, createdAt) {

        @Keep
        enum class Reason {
            TIMED_OUT, NEW_SESSION
        }
    }

    companion object {
        const val EVENT_VERSION = DEFAULT_EVENT_VERSION
    }
}
