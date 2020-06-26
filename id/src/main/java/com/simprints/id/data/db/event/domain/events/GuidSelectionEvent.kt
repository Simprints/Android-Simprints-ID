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
    listOf(EventLabel.SessionId(sessionId)),
    GuidSelectionPayload(creationTime, selectedId)) {

    @Keep
    class GuidSelectionPayload(startTime: Long,
                               val selectedId: String) : EventPayload(EventPayloadType.GUID_SELECTION, startTime)

}
