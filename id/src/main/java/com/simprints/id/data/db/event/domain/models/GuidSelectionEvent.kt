package com.simprints.id.data.db.event.domain.models

import androidx.annotation.Keep

import com.simprints.id.data.db.event.domain.models.EventType.GUID_SELECTION
import java.util.*

@Keep
data class GuidSelectionEvent(
    override val id: String = UUID.randomUUID().toString(),
    override var labels: EventLabels,
    override val payload: GuidSelectionPayload,
    override val type: EventType
) : Event(id, labels, payload, type) {

    constructor(
        createdAt: Long,
        selectedId: String,
        labels: EventLabels = EventLabels() //StopShip: to change in PAS-993
    ) : this(
        UUID.randomUUID().toString(),
        labels,
        GuidSelectionPayload(createdAt, EVENT_VERSION, selectedId),
        GUID_SELECTION)

    @Keep
    data class GuidSelectionPayload(override val createdAt: Long,
                                    override val eventVersion: Int,
                                    val selectedId: String) : EventPayload(GUID_SELECTION, eventVersion, createdAt)

    companion object {
        const val EVENT_VERSION = DEFAULT_EVENT_VERSION
    }
}
