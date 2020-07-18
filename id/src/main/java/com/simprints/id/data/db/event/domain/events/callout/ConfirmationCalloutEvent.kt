package com.simprints.id.data.db.event.domain.events.callout

import androidx.annotation.Keep
import com.simprints.id.data.db.event.domain.events.Event
import com.simprints.id.data.db.event.domain.events.EventLabel.SessionIdLabel
import com.simprints.id.data.db.event.domain.events.EventPayload
import com.simprints.id.data.db.event.domain.events.EventType
import java.util.*

@Keep
class ConfirmationCalloutEvent(
    createdAt: Long,
    projectId: String,
    selectedGuid: String,
    sessionId: String = UUID.randomUUID().toString() //StopShip: to change in PAS-993
) : Event(
    UUID.randomUUID().toString(),
    mutableListOf(SessionIdLabel(sessionId)),
    ConfirmationCalloutPayload(createdAt, DEFAULT_EVENT_VERSION, projectId, selectedGuid, sessionId)) {

    @Keep
    class ConfirmationCalloutPayload(
        createdAt: Long,
        eventVersion: Int,
        val projectId: String,
        val selectedGuid: String,
        val sessionId: String
    ) : EventPayload(EventType.CALLOUT_CONFIRMATION, eventVersion, createdAt)
}
