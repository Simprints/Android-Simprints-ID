package com.simprints.id.data.db.event.domain.events

import androidx.annotation.Keep
import java.util.*

@Keep
class EnrolmentEvent(
    createdAt: Long,
    personId: String,
    sessionId: String = UUID.randomUUID().toString() //StopShip: to change in PAS-993
) : Event(
    UUID.randomUUID().toString(),
    mutableListOf(EventLabel.SessionId(sessionId)),
    EnrolmentPayload(createdAt, DEFAULT_EVENT_VERSION, personId)) {


    @Keep
    class EnrolmentPayload(createdAt: Long,
                           eventVersion: Int,
                           val personId: String) : EventPayload(EventPayloadType.ENROLMENT, eventVersion, createdAt)
}
