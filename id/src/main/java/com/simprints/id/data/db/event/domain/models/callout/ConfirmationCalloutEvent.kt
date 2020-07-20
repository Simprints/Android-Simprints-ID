package com.simprints.id.data.db.event.domain.models.callout

import androidx.annotation.Keep
import com.simprints.id.data.db.event.domain.models.Event
import com.simprints.id.data.db.event.domain.models.EventLabel.SessionIdLabel
import com.simprints.id.data.db.event.domain.models.EventPayload
import com.simprints.id.data.db.event.domain.models.EventType
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
