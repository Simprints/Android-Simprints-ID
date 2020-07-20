package com.simprints.id.data.db.event.domain.models.callout

import androidx.annotation.Keep
import com.simprints.id.data.db.event.domain.models.Event
import com.simprints.id.data.db.event.domain.models.EventLabel.SessionIdLabel
import com.simprints.id.data.db.event.domain.models.EventPayload
import com.simprints.id.data.db.event.domain.models.EventType
import java.util.*

@Keep
class VerificationCalloutEvent(
    createdAt: Long,
    projectId: String,
    userId: String,
    moduleId: String,
    verifyGuid: String,
    metadata: String,
    sessionId: String = UUID.randomUUID().toString() //StopShip: to change in PAS-993
) : Event(
    UUID.randomUUID().toString(),
    mutableListOf(SessionIdLabel(sessionId)),
    VerificationCalloutPayload(createdAt, DEFAULT_EVENT_VERSION, projectId, userId, moduleId, verifyGuid, metadata)) {

    @Keep
    class VerificationCalloutPayload(
        createdAt: Long,
        eventVersion: Int,
        val projectId: String,
        val userId: String,
        val moduleId: String,
        val verifyGuid: String,
        val metadata: String
    ) : EventPayload(EventType.CALLOUT_VERIFICATION, eventVersion, createdAt)
}
