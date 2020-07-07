package com.simprints.id.data.db.event.domain.events

import androidx.annotation.Keep
import java.util.*

@Keep
class EnrolmentEvent(
    startTime: Long,
    personId: String,
    sessionId: String = UUID.randomUUID().toString() //StopShip: to change in PAS-993
) : Event(
    UUID.randomUUID().toString(),
    DEFAULT_EVENT_VERSION,
    listOf(EventLabel.SessionId(sessionId)),
    EnrolmentPayload(startTime, DEFAULT_EVENT_VERSION, personId)) {


    @Keep
    class EnrolmentPayload(creationTime: Long,
                           version: Int,
                           val personId: String) : EventPayload(EventPayloadType.ENROLMENT, version, creationTime)
}
