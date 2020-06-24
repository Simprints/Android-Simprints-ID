package com.simprints.id.data.db.event.domain.events

import androidx.annotation.Keep
import java.util.*

@Keep
class EnrolmentEvent(
    startTime: Long,
    personId: String,
    sessionId: String = UUID.randomUUID().toString(), //StopShip: to change in PAS-993
    sessionStartTime: Long = 0 //StopShip: to change in PAS-993
) : Event(
    UUID.randomUUID().toString(),
    listOf(EventLabel.SessionId(sessionId)),
    EnrolmentPayload(startTime, startTime - sessionStartTime, personId)) {


    @Keep
    class EnrolmentPayload(startTime: Long,
                           relativeStartTime: Long,
                           val personId: String) : EventPayload(EventPayloadType.ENROLMENT, startTime, relativeStartTime)
}
