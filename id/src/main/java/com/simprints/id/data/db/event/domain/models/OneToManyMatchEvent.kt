package com.simprints.id.data.db.event.domain.models

import androidx.annotation.Keep
import com.simprints.id.data.db.event.domain.models.EventLabel.SessionIdLabel
import com.simprints.id.data.db.event.domain.models.EventType.ONE_TO_MANY_MATCH
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
    mutableListOf(SessionIdLabel(sessionId)),
    OneToManyMatchPayload(createdAt, EVENT_VERSION, endTime, pool, result),
    ONE_TO_MANY_MATCH) {

    @Keep
    class OneToManyMatchPayload(
        createdAt: Long,
        eventVersion: Int,
        endTimeAt: Long,
        val pool: MatchPool,
        val result: List<MatchEntry>?
    ) : EventPayload(ONE_TO_MANY_MATCH, eventVersion, createdAt, endTimeAt) {

        @Keep
        class MatchPool(val type: MatchPoolType, val count: Int)

        @Keep
        enum class MatchPoolType {
            USER,
            MODULE,
            PROJECT;
        }
    }

    companion object {
        const val EVENT_VERSION = DEFAULT_EVENT_VERSION
    }
}
