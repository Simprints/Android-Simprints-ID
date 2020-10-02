package com.simprints.id.data.db.event.domain.models

import androidx.annotation.Keep
import com.simprints.id.data.db.event.domain.models.EventType.ARTIFICIAL_TERMINATION
import java.util.*

@Keep
data class ArtificialTerminationEvent(
    override val id: String = UUID.randomUUID().toString(),
    override var labels: EventLabels,
    override val payload: ArtificialTerminationPayload,
    override val type: EventType
) : Event() {

    constructor(
        createdAt: Long,
        reason: ArtificialTerminationPayload.Reason,
        labels: EventLabels = EventLabels()
    ) : this(
        UUID.randomUUID().toString(),
        labels,
        ArtificialTerminationPayload(createdAt, EVENT_VERSION, reason),
        ARTIFICIAL_TERMINATION)


    @Keep
    data class ArtificialTerminationPayload(
        override val createdAt: Long,
        override val eventVersion: Int,
        val reason: Reason,
        override val type: EventType = ARTIFICIAL_TERMINATION,
        override val endedAt: Long = 0
    ) : EventPayload() {

        @Keep
        enum class Reason {
            TIMED_OUT, NEW_SESSION
        }
    }

    companion object {
        const val EVENT_VERSION = 1
    }
}
