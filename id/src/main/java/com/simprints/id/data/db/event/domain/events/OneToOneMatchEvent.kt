package com.simprints.id.data.db.event.domain.events

import androidx.annotation.Keep
import java.util.*

@Keep
class OneToOneMatchEvent(
    startTime: Long,
    endTime: Long,
    candidateId: String,
    result: MatchEntry?,
    sessionId: String = UUID.randomUUID().toString(), //StopShip: to change in PAS-993
    sessionStartTime: Long = 0 //StopShip: to change in PAS-993
) : Event(
    UUID.randomUUID().toString(),
    listOf(EventLabel.SessionId(sessionId)),
    OneToOneMatchPayload(startTime, startTime - sessionStartTime, endTime, endTime - sessionStartTime, candidateId, result)) {

    @Keep
    class OneToOneMatchPayload(
        startTime: Long,
        relativeStartTime: Long,
        val endTime: Long,
        val relativeEndTime: Long,
        val candidateId: String,
        val result: MatchEntry?
    ) : EventPayload(EventPayloadType.ONE_TO_ONE_MATCH, startTime, relativeStartTime)
}
