package com.simprints.id.data.db.event.domain.models.callback

import androidx.annotation.Keep
import com.simprints.id.data.db.event.domain.models.Event
import com.simprints.id.data.db.event.domain.models.EventLabel
import com.simprints.id.data.db.event.domain.models.EventLabel.SessionIdLabel
import com.simprints.id.data.db.event.domain.models.EventPayload
import com.simprints.id.data.db.event.domain.models.EventType
import com.simprints.id.data.db.event.domain.models.EventType.CALLBACK_IDENTIFICATION
import java.util.*

@Keep
class IdentificationCallbackEvent(
    override val id: String = UUID.randomUUID().toString(),
    override val labels: MutableList<EventLabel>,
    override val payload: IdentificationCallbackPayload,
    override val type: EventType
) : Event(id, labels, payload, type) {

    constructor(createdAt: Long,
                sessionId: String,
                scores: List<CallbackComparisonScore>) : this(
        UUID.randomUUID().toString(),
        mutableListOf(SessionIdLabel(sessionId)),
        IdentificationCallbackPayload(createdAt, EVENT_VERSION, sessionId, scores),
        CALLBACK_IDENTIFICATION)

    @Keep
    class IdentificationCallbackPayload(
        createdAt: Long,
        eventVersion: Int,
        val sessionId: String,
        val scores: List<CallbackComparisonScore>
    ) : EventPayload(CALLBACK_IDENTIFICATION, eventVersion, createdAt)

    companion object {
        const val EVENT_VERSION = DEFAULT_EVENT_VERSION
    }
}
