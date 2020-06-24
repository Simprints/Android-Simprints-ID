package com.simprints.id.data.db.event.domain.events.callout

import androidx.annotation.Keep
import com.simprints.id.data.db.event.domain.events.Event
import com.simprints.id.data.db.event.domain.events.EventLabel
import com.simprints.id.data.db.event.domain.events.EventPayload
import com.simprints.id.data.db.event.domain.events.EventPayloadType
import java.util.*

@Keep
class EnrolmentCalloutEvent(
    startTime: Long,
    projectId: String,
    userId: String,
    moduleId: String,
    metadata: String?,
    sessionId: String = UUID.randomUUID().toString(), //StopShip: to change in PAS-993
    sessionStartTime: Long = 0 //StopShip: to change in PAS-993
) : Event(
    UUID.randomUUID().toString(),
    listOf(EventLabel.SessionId(sessionId)),
    EnrolmentCalloutPayload(startTime, startTime - sessionStartTime, projectId, userId, moduleId, metadata)) {

    @Keep
    class EnrolmentCalloutPayload(startTime: Long,
                                  relativeStartTime: Long,
                                  val projectId: String,
                                  val userId: String,
                                  val moduleId: String,
                                  val metadata: String?) : EventPayload(EventPayloadType.CALLOUT_ENROLMENT, startTime, relativeStartTime)

}
