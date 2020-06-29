package com.simprints.id.data.db.event.domain.events

import androidx.annotation.Keep
import java.util.*

@Keep
class OneToManyMatchEvent(
    startTime: Long,
    endTime: Long,
    pool: OneToManyMatchPayload.MatchPool,
    result: List<MatchEntry>?,
    sessionId: String = UUID.randomUUID().toString() //StopShip: to change in PAS-993
) : Event(
    UUID.randomUUID().toString(),
    DEFAULT_EVENT_VERSION,
    listOf(EventLabel.SessionId(sessionId)),
    OneToManyMatchPayload(startTime, DEFAULT_EVENT_VERSION, endTime, pool, result)) {

    @Keep
    class OneToManyMatchPayload(
        creationTime: Long,
        version: Int,
        val endTime: Long,
        val pool: MatchPool,
        val result: List<MatchEntry>?
    ) : EventPayload(EventPayloadType.ONE_TO_MANY_MATCH, version, creationTime) {

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
