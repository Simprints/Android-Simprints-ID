package com.simprints.id.data.db.event.domain.events

import androidx.annotation.Keep
import java.util.*

@Keep
class ArtificialTerminationEvent(
    startTime: Long,
    reason: ArtificialTerminationPayload.Reason,
    sessionId: String = UUID.randomUUID().toString(), //StopShip: to change in PAS-993
    sessionStartTime: Long = 0 //StopShip: to change in PAS-993
) : Event(
    UUID.randomUUID().toString(),
    listOf(EventLabel.SessionId(sessionId)),
    ArtificialTerminationPayload(startTime, startTime - sessionStartTime, reason)) {


    @Keep
    class ArtificialTerminationPayload(
        startTime: Long,
        relativeStartTime: Long,
        val reason: Reason
    ) : EventPayload(EventPayloadType.ARTIFICIAL_TERMINATION, startTime, relativeStartTime) {

        @Keep
        enum class Reason {
            TIMED_OUT, NEW_SESSION
        }
    }
}
