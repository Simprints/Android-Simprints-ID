package com.simprints.id.data.db.event.domain.events.callout

import androidx.annotation.Keep
import com.simprints.id.data.db.event.domain.events.Event
import com.simprints.id.data.db.event.domain.events.EventPayload
import com.simprints.id.data.db.event.domain.events.EventPayloadType
import java.util.*

@Keep
class EnrolmentLastBiometricsCalloutEvent(
    creationTime: Long,
    projectId: String,
    userId: String,
    moduleId: String,
    metadata: String?,
    sessionId: String
) : Event(
    UUID.randomUUID().toString(),
    DEFAULT_EVENT_VERSION,
    listOf(EventLabel.SessionId(sessionId)),
    EnrolmentLastBiometricsCalloutPayload(creationTime, DEFAULT_EVENT_VERSION, projectId, userId, moduleId, metadata, sessionId)) {

    @Keep
    class EnrolmentLastBiometricsCalloutPayload(creationTime: Long,
                                                version: Int,
                                                val projectId: String,
                                                val userId: String,
                                                val moduleId: String,
                                                val metadata: String?,
                                                val sessionId: String) : EventPayload(EventPayloadType.CALLOUT_LAST_BIOMETRICS, version, creationTime)

}
