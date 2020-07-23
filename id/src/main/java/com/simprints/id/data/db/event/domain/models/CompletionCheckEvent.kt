package com.simprints.id.data.db.event.domain.models

import androidx.annotation.Keep
import com.simprints.id.data.db.event.domain.models.EventLabel.SessionIdLabel
import com.simprints.id.data.db.event.domain.models.EventType.COMPLETION_CHECK
import java.util.*

@Keep
class CompletionCheckEvent(
    override val id: String = UUID.randomUUID().toString(),
    override val labels: MutableList<EventLabel>,
    override val payload: CompletionCheckPayload,
    override val type: EventType
) : Event(id, labels, payload, type) {

    constructor(
        createdAt: Long,
        completed: Boolean,
        sessionId: String = UUID.randomUUID().toString() //StopShip: to change in PAS-993
    ) : this(
        UUID.randomUUID().toString(),
        mutableListOf(SessionIdLabel(sessionId)),
        CompletionCheckPayload(createdAt, EVENT_VERSION, completed),
        COMPLETION_CHECK)

    @Keep
    class CompletionCheckPayload(createdAt: Long,
                                 eventVersion: Int,
                                 val completed: Boolean) : EventPayload(COMPLETION_CHECK, eventVersion, createdAt)

    companion object {
        const val EVENT_VERSION = DEFAULT_EVENT_VERSION
    }
}
