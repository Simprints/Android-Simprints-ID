package com.simprints.id.data.db.event.domain.events.callback

import androidx.annotation.Keep
import com.simprints.id.data.db.event.domain.events.Event
import com.simprints.id.data.db.event.domain.events.EventPayload
import com.simprints.id.data.db.event.domain.events.EventPayloadType
import java.util.*

@Keep
class EnrolmentCallbackEvent(
    creationTime: Long,
    guid: String,
    sessionId: String = UUID.randomUUID().toString() //StopShip: to change in PAS-993
) : Event(
    UUID.randomUUID().toString(),
    DEFAULT_EVENT_VERSION,
    listOf(EventLabel.SessionId(sessionId)),
    EnrolmentCallbackPayload(creationTime, DEFAULT_EVENT_VERSION, guid)) {

    class EnrolmentCallbackPayload(creationTime: Long,
                                   eventVersion: Int,
                                   val guid: String) : EventPayload(EventPayloadType.CALLBACK_ENROLMENT, eventVersion, creationTime)
}
