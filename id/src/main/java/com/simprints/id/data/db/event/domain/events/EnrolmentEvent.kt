package com.simprints.id.data.db.event.domain.events

import androidx.annotation.Keep
import com.simprints.id.data.db.event.domain.events.EventLabel.SessionIdLabel
import java.util.*

@Keep
class EnrolmentEvent(
    createdAt: Long,
    personId: String,
    sessionId: String = UUID.randomUUID().toString() //StopShip: to change in PAS-993
) : Event(
    UUID.randomUUID().toString(),
    mutableListOf(SessionIdLabel(sessionId)),
    EnrolmentPayload(createdAt, DEFAULT_EVENT_VERSION, personId)) {


    @Keep
    class EnrolmentPayload(createdAt: Long,
                           eventVersion: Int,
                           val personId: String) : EventPayload(EventType.ENROLMENT, eventVersion, createdAt)
}
