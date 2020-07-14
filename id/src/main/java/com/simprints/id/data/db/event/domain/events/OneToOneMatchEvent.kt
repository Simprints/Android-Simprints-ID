package com.simprints.id.data.db.event.domain.events

import androidx.annotation.Keep
import java.util.*

@Keep
class OneToOneMatchEvent(
    createdAt: Long,
    endTime: Long,
    candidateId: String,
    result: MatchEntry?,
    sessionId: String = UUID.randomUUID().toString() //StopShip: to change in PAS-993
) : Event(
    UUID.randomUUID().toString(),
    mutableListOf(EventLabel.SessionId(sessionId)),
    OneToOneMatchPayload(createdAt, DEFAULT_EVENT_VERSION, endTime, candidateId, result)) {

    @Keep
    class OneToOneMatchPayload(
        createdAt: Long,
        eventVersion: Int,
        val endTime: Long,
        val candidateId: String,
        val result: MatchEntry?
    ) : EventPayload(EventPayloadType.ONE_TO_ONE_MATCH, eventVersion, createdAt)
}
