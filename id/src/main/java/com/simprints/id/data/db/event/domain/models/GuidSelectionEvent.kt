package com.simprints.id.data.db.event.domain.models

import androidx.annotation.Keep
import com.simprints.id.data.db.event.domain.models.EventLabel.SessionIdLabel
import com.simprints.id.data.db.event.domain.models.EventType.GUID_SELECTION
import java.util.*

@Keep
class GuidSelectionEvent(
    override val id: String = UUID.randomUUID().toString(),
    override val labels: MutableList<EventLabel>,
    override val payload: GuidSelectionPayload,
    override val type: EventType
) : Event(id, labels, payload, type) {

    constructor(
        createdAt: Long,
        selectedId: String,
        sessionId: String = UUID.randomUUID().toString() //StopShip: to change in PAS-993
    ) : this(
        UUID.randomUUID().toString(),
        mutableListOf(SessionIdLabel(sessionId)),
        GuidSelectionPayload(createdAt, EVENT_VERSION, selectedId),
        GUID_SELECTION)

    @Keep
    class GuidSelectionPayload(override val createdAt: Long,
                               override val eventVersion: Int,
                               val selectedId: String) : EventPayload(GUID_SELECTION, eventVersion, createdAt)

    companion object {
        const val EVENT_VERSION = DEFAULT_EVENT_VERSION
    }
}
