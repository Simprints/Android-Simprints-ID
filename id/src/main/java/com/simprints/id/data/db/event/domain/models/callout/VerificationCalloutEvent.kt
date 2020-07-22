package com.simprints.id.data.db.event.domain.models.callout

import androidx.annotation.Keep
import com.simprints.id.data.db.event.domain.models.Event
import com.simprints.id.data.db.event.domain.models.EventLabel.SessionIdLabel
import com.simprints.id.data.db.event.domain.models.EventPayload
import com.simprints.id.data.db.event.domain.models.EventType.CALLOUT_VERIFICATION
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
    VerificationCalloutPayload(createdAt, EVENT_VERSION, projectId, userId, moduleId, verifyGuid, metadata),
    CALLOUT_VERIFICATION) {

    @Keep
    class VerificationCalloutPayload(
        createdAt: Long,
        eventVersion: Int,
        val projectId: String,
        val userId: String,
        val moduleId: String,
        val verifyGuid: String,
        val metadata: String
    ) : EventPayload(CALLOUT_VERIFICATION, eventVersion, createdAt)

    companion object {
        const val EVENT_VERSION = DEFAULT_EVENT_VERSION
    }
}
