package com.simprints.id.data.db.event.domain.events

import androidx.annotation.Keep
import java.util.*

@Keep
class OneToManyMatchEvent(
    createdAt: Long,
    endTime: Long,
    pool: OneToManyMatchPayload.MatchPool,
    result: List<MatchEntry>?,
    sessionId: String = UUID.randomUUID().toString() //StopShip: to change in PAS-993
) : Event(
    UUID.randomUUID().toString(),
    listOf(EventLabel.SessionId(sessionId)),
    OneToManyMatchPayload(createdAt, DEFAULT_EVENT_VERSION, endTime, pool, result)) {

    @Keep
    class OneToManyMatchPayload(
        createdAt: Long,
        eventVersion: Int,
        val endTime: Long,
        val pool: MatchPool,
        val result: List<MatchEntry>?
    ) : EventPayload(EventPayloadType.ONE_TO_MANY_MATCH, eventVersion, createdAt) {

        @Keep
        class MatchPool(val type: MatchPoolType, val count: Int)

        @Keep
        enum class MatchPoolType {
            USER,
            MODULE,
            PROJECT;
        }
    }
}
