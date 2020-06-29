package com.simprints.id.data.db.event.domain.events.callout

import androidx.annotation.Keep
import com.simprints.id.data.db.event.domain.events.Event
import com.simprints.id.data.db.event.domain.events.EventPayload
import com.simprints.id.data.db.event.domain.events.EventPayloadType
import java.util.*

@Keep
class IdentificationCalloutEvent(
    creationTime: Long,
    projectId: String,
    userId: String,
    moduleId: String,
    metadata: String?,
    sessionId: String = UUID.randomUUID().toString() //StopShip: to change in PAS-993
) : Event(
    UUID.randomUUID().toString(),
    DEFAULT_EVENT_VERSION,
    listOf(EventLabel.SessionId(sessionId)),
    IdentificationCalloutPayload(creationTime, DEFAULT_EVENT_VERSION, projectId, userId, moduleId, metadata)) {

    @Keep
    class IdentificationCalloutPayload(
        creationTime: Long,
        version: Int,
        val projectId: String,
        val userId: String,
        val moduleId: String,
        val metadata: String?
    ) : EventPayload(EventPayloadType.CALLOUT_IDENTIFICATION, version, creationTime)
}
