package com.simprints.id.data.db.event.domain.events.callback

import androidx.annotation.Keep
import com.simprints.id.data.db.event.domain.events.Event
import com.simprints.id.data.db.event.domain.events.EventLabel
import com.simprints.id.data.db.event.domain.events.EventPayload
import com.simprints.id.data.db.event.domain.events.EventPayloadType
import java.util.*

@Keep
class VerificationCallbackEvent(
    startTime: Long,
    score: CallbackComparisonScore,
    sessionId: String = UUID.randomUUID().toString(), //StopShip: to change in PAS-993
    sessionStartTime: Long = 0 //StopShip: to change in PAS-993
) : Event(
    UUID.randomUUID().toString(),
    listOf(EventLabel.SessionId(sessionId)),
    VerificationCallbackPayload(startTime, sessionStartTime - startTime, score)) {

    @Keep
    class VerificationCallbackPayload(
        startTime: Long,
        relativeStartTime: Long,
        val score: CallbackComparisonScore
    ) : EventPayload(EventPayloadType.CALLBACK_VERIFICATION, startTime, relativeStartTime)
}
