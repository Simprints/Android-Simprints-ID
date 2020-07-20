package com.simprints.id.data.db.event.domain.models.callout

import androidx.annotation.Keep
import com.simprints.id.data.db.event.domain.models.Event
import com.simprints.id.data.db.event.domain.models.EventLabel.SessionIdLabel
import com.simprints.id.data.db.event.domain.models.EventPayload
import com.simprints.id.data.db.event.domain.models.EventType
import java.util.*

@Keep
class IdentificationCalloutEvent(
    createdAt: Long,
    projectId: String,
    userId: String,
    moduleId: String,
    metadata: String?,
    sessionId: String = UUID.randomUUID().toString() //StopShip: to change in PAS-993
) : Event(
    UUID.randomUUID().toString(),
    mutableListOf(SessionIdLabel(sessionId)),
    IdentificationCalloutPayload(createdAt, DEFAULT_EVENT_VERSION, projectId, userId, moduleId, metadata)) {

    @Keep
    class IdentificationCalloutPayload(
        createdAt: Long,
        eventVersion: Int,
        val projectId: String,
        val userId: String,
        val moduleId: String,
        val metadata: String?
    ) : EventPayload(EventType.CALLOUT_IDENTIFICATION, eventVersion, createdAt)
}
