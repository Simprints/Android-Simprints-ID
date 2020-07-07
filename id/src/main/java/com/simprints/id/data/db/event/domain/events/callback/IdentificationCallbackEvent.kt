package com.simprints.id.data.db.event.domain.events.callback

import androidx.annotation.Keep
import com.simprints.id.data.db.event.domain.events.Event
import com.simprints.id.data.db.event.domain.events.EventLabel
import com.simprints.id.data.db.event.domain.events.EventPayload
import com.simprints.id.data.db.event.domain.events.EventPayloadType
import java.util.*

@Keep
class IdentificationCallbackEvent(
    creationTime: Long,
    sessionId: String,
    scores: List<CallbackComparisonScore>
) : Event(
    UUID.randomUUID().toString(),
    listOf(EventLabel.SessionId(sessionId)),
    IdentificationCallbackPayload(creationTime, sessionId, scores)) {

    @Keep
    class IdentificationCallbackPayload(
        creationTime: Long,
        val sessionId: String,
        val scores: List<CallbackComparisonScore>
    ) : EventPayload(EventPayloadType.CALLBACK_IDENTIFICATION, creationTime)

}
