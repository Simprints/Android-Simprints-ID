package com.simprints.id.data.db.event.domain.events

import androidx.annotation.Keep
import java.util.*

@Keep
class OneToOneMatchEvent(
    startTime: Long,
    endTime: Long,
    candidateId: String,
    result: MatchEntry?,
    sessionId: String = UUID.randomUUID().toString() //StopShip: to change in PAS-993
) : Event(
    UUID.randomUUID().toString(),
    listOf(EventLabel.SessionId(sessionId)),
    OneToOneMatchPayload(startTime, endTime, candidateId, result)) {

    @Keep
    class OneToOneMatchPayload(
        startTime: Long,
        val endTime: Long,
        val candidateId: String,
        val result: MatchEntry?
    ) : EventPayload(EventPayloadType.ONE_TO_ONE_MATCH, startTime)
}
