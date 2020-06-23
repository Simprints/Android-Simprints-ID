package com.simprints.id.data.db.session.domain.models.events

import androidx.annotation.Keep
import java.util.*

@Keep
class ArtificialTerminationEvent(
    startTime: Long,
    reason: ArtificialTerminationPayload.Reason,
    sessionId: String = UUID.randomUUID().toString() //StopShip: to change in PAS-993
) : Event(
    UUID.randomUUID().toString(),
    listOf(EventLabel.SessionId(sessionId)),
    ArtificialTerminationPayload(startTime, reason)) {


    @Keep
    class ArtificialTerminationPayload(
        val startTime: Long,
        val reason: Reason
    ) : EventPayload(EventPayloadType.ARTIFICIAL_TERMINATION) {

        @Keep
        enum class Reason {
            TIMED_OUT, NEW_SESSION
        }
    }
}
