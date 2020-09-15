package com.simprints.id.data.db.event.domain.models

import androidx.annotation.Keep

import com.simprints.id.data.db.event.domain.models.EventType.ONE_TO_ONE_MATCH
import com.simprints.id.data.db.event.local.models.DbEvent.Companion.DEFAULT_EVENT_VERSION
import java.util.*

@Keep
data class OneToOneMatchEvent(
    override val id: String = UUID.randomUUID().toString(),
    override var labels: EventLabels,
    override val payload: OneToOneMatchPayload,
    override val type: EventType
) : Event() {

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
    data class OneToOneMatchPayload(
        override val createdAt: Long,
        override val eventVersion: Int,
        override var endedAt: Long,
        val candidateId: String,
        val matcher: Matcher,
        val result: MatchEntry?,
        override val type: EventType = ONE_TO_ONE_MATCH
    ) : EventPayload()

    companion object {
        const val EVENT_VERSION = 1
    }
}
