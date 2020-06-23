package com.simprints.id.data.db.session.domain.models.events

import androidx.annotation.Keep
import java.util.*

@Keep
class EnrolmentEvent(
    startTime: Long,
    personId: String,
    sessionId: String = UUID.randomUUID().toString() //StopShip: to change in PAS-993
) : Event(
    UUID.randomUUID().toString(),
    listOf(EventLabel.SessionId(sessionId)),
    EnrolmentPayload(startTime, personId)) {


    @Keep
    class EnrolmentPayload(val startTime: Long,
                           val personId: String) : EventPayload(EventPayloadType.ENROLMENT)
}
