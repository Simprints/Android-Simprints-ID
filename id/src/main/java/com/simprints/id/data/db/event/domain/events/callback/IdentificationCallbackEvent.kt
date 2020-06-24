package com.simprints.id.data.db.event.domain.events.callback

import androidx.annotation.Keep
import com.simprints.id.data.db.event.domain.events.Event
import com.simprints.id.data.db.event.domain.events.EventLabel
import com.simprints.id.data.db.event.domain.events.EventPayload
import com.simprints.id.data.db.event.domain.events.EventPayloadType
import java.util.*

@Keep
class IdentificationCallbackEvent(
    startTime: Long,
    sessionId: String,
    scores: List<CallbackComparisonScore>,
    sessionStartTime: Long = 0 //StopShip: to change in PAS-993
) : Event(
    UUID.randomUUID().toString(),
    listOf(EventLabel.SessionId(sessionId)),
    IdentificationCallbackPayload(startTime, startTime - sessionStartTime, sessionId, scores)) {

    @Keep
    class IdentificationCallbackPayload(
        startTime: Long,
        relativeStartTime: Long,
        val sessionId: String,
        val scores: List<CallbackComparisonScore>
    ) : EventPayload(EventPayloadType.CALLBACK_IDENTIFICATION, startTime, relativeStartTime)

}
