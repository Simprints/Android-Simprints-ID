package com.simprints.id.data.db.event.domain.events.callback

import androidx.annotation.Keep
import com.simprints.id.data.db.event.domain.events.Event
import com.simprints.id.data.db.event.domain.events.EventLabel
import com.simprints.id.data.db.event.domain.events.EventPayload
import com.simprints.id.data.db.event.domain.events.EventPayloadType
import java.util.*

@Keep
class EnrolmentCallbackEvent(
    startTime: Long,
    guid: String,
    sessionId: String = UUID.randomUUID().toString(), //StopShip: to change in PAS-993
    sessionStartTime: Long = 0 //StopShip: to change in PAS-993
) : Event(
    UUID.randomUUID().toString(),
    listOf(EventLabel.SessionId(sessionId)),
    EnrolmentCallbackPayload(startTime, startTime - sessionStartTime, guid)) {

    class EnrolmentCallbackPayload(startTime: Long,
                                   relativeStartTime: Long,
                                   val guid: String) : EventPayload(EventPayloadType.CALLBACK_ENROLMENT, startTime, relativeStartTime)
}
