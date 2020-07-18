package com.simprints.id.data.db.event.domain.events.callback

import androidx.annotation.Keep
import com.simprints.id.data.db.event.domain.events.Event
import com.simprints.id.data.db.event.domain.events.EventLabel.SessionIdLabel
import com.simprints.id.data.db.event.domain.events.EventPayload
import com.simprints.id.data.db.event.domain.events.EventType
import java.util.*

@Keep
class ConfirmationCallbackEvent(
    createdAt: Long,
    identificationOutcome: Boolean,
    sessionId: String = UUID.randomUUID().toString() //StopShip: to change in PAS-993
) : Event(
    UUID.randomUUID().toString(),
    mutableListOf(SessionIdLabel(sessionId)),
    ConfirmationCallbackPayload(createdAt, DEFAULT_EVENT_VERSION, identificationOutcome)) {

    class ConfirmationCallbackPayload(createdAt: Long,
                                      eventVersion: Int,
                                      val identificationOutcome: Boolean)
        : EventPayload(EventType.CALLBACK_CONFIRMATION, eventVersion, createdAt)
}
