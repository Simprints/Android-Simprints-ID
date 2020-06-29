package com.simprints.id.data.db.event.domain.events

import androidx.annotation.Keep
import java.util.*

@Keep
class ArtificialTerminationEvent(
    creationTime: Long,
    reason: ArtificialTerminationPayload.Reason,
    sessionId: String = UUID.randomUUID().toString() //StopShip: to change in PAS-993
) : Event(
    UUID.randomUUID().toString(),
    listOf(EventLabel.SessionId(sessionId)),
    ArtificialTerminationPayload(creationTime, reason)) {


    @Keep
    class ArtificialTerminationPayload(
        creationTime: Long,
        val reason: Reason
    ) : EventPayload(EventPayloadType.ARTIFICIAL_TERMINATION, creationTime) {

        @Keep
        enum class Reason {
            TIMED_OUT, NEW_SESSION
        }
    }
}
