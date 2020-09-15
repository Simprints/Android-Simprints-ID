package com.simprints.id.data.db.event.domain.models

import androidx.annotation.Keep

import com.simprints.id.data.db.event.domain.models.EventType.ONE_TO_MANY_MATCH
import com.simprints.id.data.db.event.local.models.DbEvent.Companion.DEFAULT_EVENT_VERSION
import java.util.*

@Keep
data class OneToManyMatchEvent(
    override val id: String = UUID.randomUUID().toString(),
    override var labels: EventLabels,
    override val payload: OneToManyMatchPayload,
    override val type: EventType
) : Event() {

    constructor(
        createdAt: Long,
        endTime: Long,
        pool: OneToManyMatchPayload.MatchPool,
        matcher: Matcher,
        result: List<MatchEntry>?,
        labels: EventLabels = EventLabels() //StopShip: to change in PAS-993
    ) : this(
        UUID.randomUUID().toString(),
        labels,
        OneToManyMatchPayload(createdAt, EVENT_VERSION, endTime, pool, matcher, result),
        ONE_TO_MANY_MATCH)

    @Keep
    data class OneToManyMatchPayload(
        override val createdAt: Long,
        override val eventVersion: Int,
        override var endedAt: Long,
        val pool: MatchPool,
        val matcher: Matcher,
        val result: List<MatchEntry>?,
        override val type: EventType = ONE_TO_MANY_MATCH
    ) : EventPayload() {

        @Keep
        data class MatchPool(val type: MatchPoolType, val count: Int)

        @Keep
        enum class MatchPoolType {
            USER,
            MODULE,
            PROJECT;
        }
    }

    companion object {
        const val EVENT_VERSION = 1
    }
}
