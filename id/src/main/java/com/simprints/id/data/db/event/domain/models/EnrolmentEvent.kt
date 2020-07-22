package com.simprints.id.data.db.event.domain.models

import androidx.annotation.Keep
import com.simprints.id.data.db.event.domain.models.EventLabel.SessionIdLabel
import com.simprints.id.data.db.event.domain.models.EventType.ENROLMENT
import java.util.*

@Keep
class EnrolmentEvent(
    createdAt: Long,
    personId: String,
    sessionId: String = UUID.randomUUID().toString() //StopShip: to change in PAS-993
) : Event(
    UUID.randomUUID().toString(),
    mutableListOf(SessionIdLabel(sessionId)),
    EnrolmentPayload(createdAt, EVENT_VERSION, personId),
    ENROLMENT) {


    @Keep
    class EnrolmentPayload(createdAt: Long,
                           eventVersion: Int,
                           val personId: String) : EventPayload(ENROLMENT, eventVersion, createdAt)

    companion object {
        const val EVENT_VERSION = DEFAULT_EVENT_VERSION
    }
}
