package com.simprints.id.data.db.event.domain.models

import androidx.annotation.Keep

import com.simprints.id.data.db.event.domain.models.EventType.ONE_TO_ONE_MATCH
import java.util.*

@Keep
class OneToOneMatchEvent(
    override val id: String = UUID.randomUUID().toString(),
    override var labels: EventLabels,
    override val payload: OneToOneMatchPayload,
    override val type: EventType
) : Event(id, labels, payload, type) {

    constructor(
        createdAt: Long,
        endTime: Long,
        candidateId: String,
        matcher: Matcher,
        result: MatchEntry?,
        labels: EventLabels = EventLabels() //StopShip: to change in PAS-993
    ) : this(
        UUID.randomUUID().toString(),
        labels,
        OneToOneMatchPayload(createdAt, EVENT_VERSION, endTime, candidateId, matcher, result),
        ONE_TO_ONE_MATCH)

    @Keep
    class OneToOneMatchPayload(
        override val createdAt: Long,
        override val eventVersion: Int,
        override var endedAt: Long,
        val candidateId: String,
        val matcher: Matcher,
        val result: MatchEntry?
    ) : EventPayload(ONE_TO_ONE_MATCH, eventVersion, createdAt, endedAt)

    companion object {
        const val EVENT_VERSION = DEFAULT_EVENT_VERSION
    }
}
