package com.simprints.id.data.db.event.domain.events

import androidx.annotation.Keep
import java.util.*

@Keep
class GuidSelectionEvent(
    createdAt: Long,
    selectedId: String,
    sessionId: String = UUID.randomUUID().toString() //StopShip: to change in PAS-993
) : Event(
    UUID.randomUUID().toString(),
    listOf(EventLabel.SessionId(sessionId)),
    GuidSelectionPayload(createdAt, DEFAULT_EVENT_VERSION, selectedId)) {

    @Keep
    class GuidSelectionPayload(creationTime: Long,
                               eventVersion: Int,
                               val selectedId: String) : EventPayload(EventPayloadType.GUID_SELECTION, eventVersion, creationTime)

}
