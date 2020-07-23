package com.simprints.id.data.db.event.domain.models.callback

import androidx.annotation.Keep
import com.simprints.id.data.db.event.domain.models.Event
import com.simprints.id.data.db.event.domain.models.EventLabel
import com.simprints.id.data.db.event.domain.models.EventLabel.SessionIdLabel
import com.simprints.id.data.db.event.domain.models.EventPayload
import com.simprints.id.data.db.event.domain.models.EventType
import com.simprints.id.data.db.event.domain.models.EventType.CALLBACK_REFUSAL
import java.util.*

@Keep
class RefusalCallbackEvent(
    override val id: String = UUID.randomUUID().toString(),
    override val labels: MutableList<EventLabel>,
    override val payload: RefusalCallbackPayload,
    override val type: EventType
) : Event(id, labels, payload, type) {

    constructor(
        createdAt: Long,
        reason: String,
        extra: String,
        sessionId: String = UUID.randomUUID().toString() //StopShip: to change in PAS-993
    ) : this(
            UUID.randomUUID().toString(),
            mutableListOf<EventLabel>(SessionIdLabel(sessionId)),
            RefusalCallbackPayload(createdAt, DEFAULT_EVENT_VERSION, reason, extra),
            CALLBACK_REFUSAL)

    @Keep
    class RefusalCallbackPayload(createdAt: Long,
                                 eventVersion: Int,
                                 val reason: String,
                                 val extra: String) : EventPayload(CALLBACK_REFUSAL, eventVersion, createdAt)

    companion object {
        const val EVENT_VERSION = DEFAULT_EVENT_VERSION
    }
}
