package com.simprints.id.data.db.event.domain.models.callout

import androidx.annotation.Keep
import com.simprints.id.data.db.event.domain.models.Event
import com.simprints.id.data.db.event.domain.models.EventLabel.SessionIdLabel
import com.simprints.id.data.db.event.domain.models.EventPayload
import com.simprints.id.data.db.event.domain.models.EventType.CALLOUT_ENROLMENT
import java.util.*

@Keep
class EnrolmentCalloutEvent(
    createdAt: Long,
    projectId: String,
    userId: String,
    moduleId: String,
    metadata: String?,
    sessionId: String = UUID.randomUUID().toString() //StopShip: to change in PAS-993
) : Event(
    UUID.randomUUID().toString(),
    mutableListOf(SessionIdLabel(sessionId)),
    EnrolmentCalloutPayload(createdAt, EVENT_VERSION, projectId, userId, moduleId, metadata),
    CALLOUT_ENROLMENT) {

    @Keep
    class EnrolmentCalloutPayload(createdAt: Long,
                                  eventVersion: Int,
                                  val projectId: String,
                                  val userId: String,
                                  val moduleId: String,
                                  val metadata: String?) : EventPayload(CALLOUT_ENROLMENT, eventVersion, createdAt)

    companion object {
        const val EVENT_VERSION = DEFAULT_EVENT_VERSION
    }

}
