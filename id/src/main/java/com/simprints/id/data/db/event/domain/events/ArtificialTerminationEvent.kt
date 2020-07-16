package com.simprints.id.data.db.event.domain.events

import androidx.annotation.Keep
import com.simprints.id.data.db.event.domain.events.EventLabel.SessionIdLabel
import java.util.*

@Keep
class ArtificialTerminationEvent(
    createdAt: Long,
    reason: ArtificialTerminationPayload.Reason,
    sessionId: String = UUID.randomUUID().toString() //StopShip: to change in PAS-993
) : Event(
    UUID.randomUUID().toString(),
    mutableListOf(SessionIdLabel(sessionId)),
    ArtificialTerminationPayload(createdAt, DEFAULT_EVENT_VERSION, reason)) {


    @Keep
    class ArtificialTerminationPayload(
        createdAt: Long,
        eventVersion: Int,
        val reason: Reason
    ) : EventPayload(EventPayloadType.ARTIFICIAL_TERMINATION, eventVersion, createdAt) {

        @Keep
        enum class Reason {
            TIMED_OUT, NEW_SESSION
        }
    }
}
