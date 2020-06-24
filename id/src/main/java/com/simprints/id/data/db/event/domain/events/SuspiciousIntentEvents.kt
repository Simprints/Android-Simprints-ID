package com.simprints.id.data.db.event.domain.events

import androidx.annotation.Keep
import java.util.*

@Keep
class SuspiciousIntentEvent(
    startTime: Long,
    unexpectedExtras: Map<String, Any?>,
    sessionId: String = UUID.randomUUID().toString(), //StopShip: to change in PAS-993
    sessionStartTime: Long = 0 //StopShip: to change in PAS-993
) : Event(
    UUID.randomUUID().toString(),
    listOf(EventLabel.SessionId(sessionId)),
    SuspiciousIntentPayload(startTime, startTime - sessionStartTime, unexpectedExtras)) {

    @Keep
    class SuspiciousIntentPayload(
        startTime: Long,
        relativeStartTime: Long,
        val unexpectedExtras: Map<String, Any?>
    ) : EventPayload(EventPayloadType.SUSPICIOUS_INTENT, startTime, relativeStartTime)
}
