package com.simprints.id.data.db.event.domain.events

import androidx.annotation.Keep
import java.util.*

@Keep
class OneToManyMatchEvent(
    startTime: Long,
    endTime: Long,
    pool: OneToManyMatchPayload.MatchPool,
    result: List<MatchEntry>?,
    sessionId: String = UUID.randomUUID().toString(), //StopShip: to change in PAS-993
    sessionStartTime: Long = 0 //StopShip: to change in PAS-993
) : Event(
    UUID.randomUUID().toString(),
    listOf(EventLabel.SessionId(sessionId)),
    OneToManyMatchPayload(startTime, startTime - sessionStartTime, endTime, endTime - sessionStartTime, pool, result)) {

    @Keep
    class OneToManyMatchPayload(
        startTime: Long,
        relativeStartTime: Long,
        val endTime: Long,
        val relativeEndTime: Long,
        val pool: MatchPool,
        val result: List<MatchEntry>?
    ) : EventPayload(EventPayloadType.ONE_TO_MANY_MATCH, startTime, relativeStartTime) {

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
