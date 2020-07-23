package com.simprints.id.data.db.event.domain.models

import androidx.annotation.Keep
import com.simprints.id.data.db.event.domain.models.EventLabel.SessionIdLabel
import com.simprints.id.data.db.event.domain.models.EventType.INVALID_INTENT
import java.util.*

@Keep
class InvalidIntentEvent(
    override val id: String = UUID.randomUUID().toString(),
    override val labels: MutableList<EventLabel>,
    override val payload: InvalidIntentPayload,
    override val type: EventType
) : Event(id, labels, payload, type) {

    constructor(
        creationTime: Long,
        action: String,
        extras: Map<String, Any?>,
        sessionId: String = UUID.randomUUID().toString() //StopShip: to change in PAS-993
    ) : this(
        UUID.randomUUID().toString(),
        mutableListOf<EventLabel>(SessionIdLabel(sessionId)),
        InvalidIntentPayload(creationTime, EVENT_VERSION, action, extras),
        INVALID_INTENT)


    @Keep
    class InvalidIntentPayload(createdAt: Long,
                               eventVersion: Int,
                               val action: String,
                               val extras: Map<String, Any?>) : EventPayload(INVALID_INTENT, eventVersion, createdAt)

    companion object {
        const val EVENT_VERSION = DEFAULT_EVENT_VERSION
    }
}
