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
    DEFAULT_EVENT_VERSION,
    listOf(EventLabel.SessionId(sessionId)),
    ArtificialTerminationPayload(creationTime, DEFAULT_EVENT_VERSION, reason)) {


    @Keep
    class ArtificialTerminationPayload(
        creationTime: Long,
        version: Int,
        val reason: Reason
    ) : EventPayload(EventPayloadType.ARTIFICIAL_TERMINATION, version, creationTime) {

        @Keep
        enum class Reason {
            TIMED_OUT, NEW_SESSION
        }
    }
}
