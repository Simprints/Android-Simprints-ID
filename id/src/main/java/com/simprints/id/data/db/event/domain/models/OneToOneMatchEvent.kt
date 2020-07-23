package com.simprints.id.data.db.event.domain.models

import androidx.annotation.Keep
import com.simprints.id.data.db.event.domain.models.EventLabel.SessionIdLabel
import com.simprints.id.data.db.event.domain.models.EventType.ONE_TO_ONE_MATCH
import java.util.*

@Keep
class OneToOneMatchEvent(
    override val id: String = UUID.randomUUID().toString(),
    override val labels: MutableList<EventLabel>,
    override val payload: OneToOneMatchPayload,
    override val type: EventType
) : Event(id, labels, payload, type) {

    constructor(
        createdAt: Long,
        endTime: Long,
        candidateId: String,
        result: MatchEntry?,
        sessionId: String = UUID.randomUUID().toString() //StopShip: to change in PAS-993
    ) : this(
        UUID.randomUUID().toString(),
        mutableListOf<EventLabel>(SessionIdLabel(sessionId)),
        OneToOneMatchPayload(createdAt, EVENT_VERSION, endTime, candidateId, result),
        ONE_TO_ONE_MATCH)

    @Keep
    class OneToOneMatchPayload(
        override val createdAt: Long,
        override val eventVersion: Int,
        override val endedAt: Long,
        val candidateId: String,
        val result: MatchEntry?
    ) : EventPayload(ONE_TO_ONE_MATCH, eventVersion, createdAt, endedAt)

    companion object {
        const val EVENT_VERSION = DEFAULT_EVENT_VERSION
    }
}
