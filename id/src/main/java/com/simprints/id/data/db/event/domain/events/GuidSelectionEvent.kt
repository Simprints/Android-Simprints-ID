package com.simprints.id.data.db.event.domain.events

import androidx.annotation.Keep
import com.simprints.id.data.db.event.domain.events.EventLabel.SessionIdLabel
import java.util.*

@Keep
class GuidSelectionEvent(
    createdAt: Long,
    selectedId: String,
    sessionId: String = UUID.randomUUID().toString() //StopShip: to change in PAS-993
) : Event(
    UUID.randomUUID().toString(),
    mutableListOf(SessionIdLabel(sessionId)),
    GuidSelectionPayload(createdAt, DEFAULT_EVENT_VERSION, selectedId)) {

    @Keep
    class GuidSelectionPayload(creationTime: Long,
                               eventVersion: Int,
                               val selectedId: String) : EventPayload(EventType.GUID_SELECTION, eventVersion, creationTime)

}
