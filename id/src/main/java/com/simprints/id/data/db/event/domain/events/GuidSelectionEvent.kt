package com.simprints.id.data.db.event.domain.events

import androidx.annotation.Keep
import java.util.*

@Keep
class GuidSelectionEvent(
    creationTime: Long,
    selectedId: String,
    sessionId: String = UUID.randomUUID().toString() //StopShip: to change in PAS-993
) : Event(
    UUID.randomUUID().toString(),
    DEFAULT_EVENT_VERSION,
    listOf(EventLabel.SessionId(sessionId)),
    GuidSelectionPayload(creationTime, DEFAULT_EVENT_VERSION, selectedId)) {

    @Keep
    class GuidSelectionPayload(creationTime: Long,
                               version: Int,
                               val selectedId: String) : EventPayload(EventPayloadType.GUID_SELECTION, version, creationTime)

}
