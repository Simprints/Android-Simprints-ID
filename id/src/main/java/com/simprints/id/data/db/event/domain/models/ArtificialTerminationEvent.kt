package com.simprints.id.data.db.event.domain.models

import androidx.annotation.Keep
import com.simprints.id.data.db.event.domain.models.EventLabel.SessionIdLabel
import com.simprints.id.data.db.event.domain.models.EventType.ARTIFICIAL_TERMINATION
import java.util.*

@Keep
class ArtificialTerminationEvent(
    createdAt: Long,
    reason: ArtificialTerminationPayload.Reason,
    sessionId: String = UUID.randomUUID().toString() //StopShip: to change in PAS-993
) : Event(
    UUID.randomUUID().toString(),
    mutableListOf(SessionIdLabel(sessionId)),
    ArtificialTerminationPayload(createdAt, EVENT_VERSION, reason),
    ARTIFICIAL_TERMINATION) {


    @Keep
    class ArtificialTerminationPayload(
        createdAt: Long,
        eventVersion: Int,
        val reason: Reason
    ) : EventPayload(ARTIFICIAL_TERMINATION, eventVersion, createdAt) {

        @Keep
        enum class Reason {
            TIMED_OUT, NEW_SESSION
        }
    }

    companion object {
        const val EVENT_VERSION = DEFAULT_EVENT_VERSION
    }
}
