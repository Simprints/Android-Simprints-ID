package com.simprints.id.data.db.session.domain.models.events

import androidx.annotation.Keep
import java.util.*

@Keep
class GuidSelectionEvent(
    startTime: Long,
    selectedId: String,
    sessionId: String = UUID.randomUUID().toString() //StopShip: to change in PAS-993
) : Event(
    UUID.randomUUID().toString(),
    listOf(EventLabel.SessionId(sessionId)),
    GuidSelectionPayload(startTime, selectedId)) {

    @Keep
    class GuidSelectionPayload(val startTime: Long,
                               val selectedId: String) : EventPayload(EventPayloadType.GUID_SELECTION)

}
