package com.simprints.id.data.db.event.domain.events.callback

import androidx.annotation.Keep
import com.simprints.id.data.db.event.domain.events.Event
import com.simprints.id.data.db.event.domain.events.EventLabel.SessionIdLabel
import com.simprints.id.data.db.event.domain.events.EventPayload
import com.simprints.id.data.db.event.domain.events.EventType
import java.util.*

@Keep
class IdentificationCallbackEvent(
    createdAt: Long,
    sessionId: String,
    scores: List<CallbackComparisonScore>
) : Event(
    UUID.randomUUID().toString(),
    mutableListOf(SessionIdLabel(sessionId)),
    IdentificationCallbackPayload(createdAt, DEFAULT_EVENT_VERSION, sessionId, scores)) {

    @Keep
    class IdentificationCallbackPayload(
        createdAt: Long,
        eventVersion: Int,
        val sessionId: String,
        val scores: List<CallbackComparisonScore>
    ) : EventPayload(EventType.CALLBACK_IDENTIFICATION, eventVersion, createdAt)
}
