package com.simprints.id.data.db.event.domain.models.callback

import androidx.annotation.Keep
import com.simprints.id.data.db.event.domain.models.Event
import com.simprints.id.data.db.event.domain.models.EventLabel.SessionIdLabel
import com.simprints.id.data.db.event.domain.models.EventPayload
import com.simprints.id.data.db.event.domain.models.EventType
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
