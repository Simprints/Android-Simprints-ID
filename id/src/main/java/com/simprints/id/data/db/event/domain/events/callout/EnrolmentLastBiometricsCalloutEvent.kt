package com.simprints.id.data.db.event.domain.events.callout

import androidx.annotation.Keep
import com.simprints.id.data.db.event.domain.events.Event
import com.simprints.id.data.db.event.domain.events.EventLabel.SessionIdLabel
import com.simprints.id.data.db.event.domain.events.EventPayload
import com.simprints.id.data.db.event.domain.events.EventType
import java.util.*

@Keep
class EnrolmentLastBiometricsCalloutEvent(
    createdAt: Long,
    projectId: String,
    userId: String,
    moduleId: String,
    metadata: String?,
    sessionId: String
) : Event(
    UUID.randomUUID().toString(),
    mutableListOf(SessionIdLabel(sessionId)),
    EnrolmentLastBiometricsCalloutPayload(createdAt, DEFAULT_EVENT_VERSION, projectId, userId, moduleId, metadata, sessionId)) {

    @Keep
    class EnrolmentLastBiometricsCalloutPayload(createdAt: Long,
                                                eventVersion: Int,
                                                val projectId: String,
                                                val userId: String,
                                                val moduleId: String,
                                                val metadata: String?,
                                                val sessionId: String) : EventPayload(EventType.CALLOUT_LAST_BIOMETRICS, eventVersion, createdAt)

}
