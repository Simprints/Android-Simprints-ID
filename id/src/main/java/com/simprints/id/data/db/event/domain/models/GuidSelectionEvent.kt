package com.simprints.id.data.db.event.domain.models

import androidx.annotation.Keep
import com.simprints.id.data.db.event.domain.models.EventLabel.SessionIdLabel
import com.simprints.id.data.db.event.domain.models.EventType.GUID_SELECTION
import java.util.*

@Keep
class GuidSelectionEvent(
    createdAt: Long,
    selectedId: String,
    sessionId: String = UUID.randomUUID().toString() //StopShip: to change in PAS-993
) : Event(
    UUID.randomUUID().toString(),
    mutableListOf(SessionIdLabel(sessionId)),
    GuidSelectionPayload(createdAt, EVENT_VERSION, selectedId),
    GUID_SELECTION) {

    @Keep
    class GuidSelectionPayload(creationTime: Long,
                               eventVersion: Int,
                               val selectedId: String) : EventPayload(GUID_SELECTION, eventVersion, creationTime)

    companion object {
        const val EVENT_VERSION = DEFAULT_EVENT_VERSION
    }
}
