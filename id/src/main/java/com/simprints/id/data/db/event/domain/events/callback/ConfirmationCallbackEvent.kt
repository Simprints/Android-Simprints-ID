package com.simprints.id.data.db.event.domain.events.callback

import androidx.annotation.Keep
import com.simprints.id.data.db.event.domain.events.Event
import com.simprints.id.data.db.event.domain.events.EventLabel
import com.simprints.id.data.db.event.domain.events.EventPayload
import com.simprints.id.data.db.event.domain.events.EventPayloadType
import java.util.*

@Keep
class ConfirmationCallbackEvent(
    creationTime: Long,
    identificationOutcome: Boolean,
    sessionId: String = UUID.randomUUID().toString() //StopShip: to change in PAS-993
) : Event(
    UUID.randomUUID().toString(),
    listOf(EventLabel.SessionId(sessionId)),
    ConfirmationCallbackPayload(creationTime, identificationOutcome)) {

    class ConfirmationCallbackPayload(creationTime: Long,
                                      val identificationOutcome: Boolean)
        : EventPayload(EventPayloadType.CALLBACK_CONFIRMATION, creationTime)
}
