package com.simprints.id.data.db.event.domain.events

import androidx.annotation.Keep
import java.util.*

@Keep
class GuidSelectionEvent(
    startTime: Long,
    selectedId: String,
    sessionId: String = UUID.randomUUID().toString(), //StopShip: to change in PAS-993
    sessionStartTime: Long = 0 //StopShip: to change in PAS-993
) : Event(
    UUID.randomUUID().toString(),
    listOf(EventLabel.SessionId(sessionId)),
    GuidSelectionPayload(startTime, startTime - sessionStartTime, selectedId)) {

    @Keep
    class GuidSelectionPayload(startTime: Long,
                               relativeStartTime: Long,
                               val selectedId: String) : EventPayload(EventPayloadType.GUID_SELECTION, startTime, relativeStartTime)

}
