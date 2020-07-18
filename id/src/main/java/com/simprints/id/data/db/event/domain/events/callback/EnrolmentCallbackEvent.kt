package com.simprints.id.data.db.event.domain.events.callback

import androidx.annotation.Keep
import com.simprints.id.data.db.event.domain.events.Event
import com.simprints.id.data.db.event.domain.events.EventLabel.SessionIdLabel
import com.simprints.id.data.db.event.domain.events.EventPayload
import com.simprints.id.data.db.event.domain.events.EventType
import java.util.*

@Keep
class EnrolmentCallbackEvent(
    createdAt: Long,
    guid: String,
    sessionId: String = UUID.randomUUID().toString() //StopShip: to change in PAS-993
) : Event(
    UUID.randomUUID().toString(),
    mutableListOf(SessionIdLabel(sessionId)),
    EnrolmentCallbackPayload(createdAt, DEFAULT_EVENT_VERSION, guid)) {

    class EnrolmentCallbackPayload(createdAt: Long,
                                   eventVersion: Int,
                                   val guid: String) : EventPayload(EventType.CALLBACK_ENROLMENT, eventVersion, createdAt)
}
