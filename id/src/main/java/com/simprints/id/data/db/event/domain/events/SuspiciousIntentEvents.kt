package com.simprints.id.data.db.event.domain.events

import androidx.annotation.Keep
import java.util.*

@Keep
class SuspiciousIntentEvent(
    creationTime: Long,
    unexpectedExtras: Map<String, Any?>,
    sessionId: String = UUID.randomUUID().toString() //StopShip: to change in PAS-993
) : Event(
    UUID.randomUUID().toString(),
    listOf(EventLabel.SessionId(sessionId)),
    SuspiciousIntentPayload(creationTime, unexpectedExtras)) {

    @Keep
    class SuspiciousIntentPayload(
        creationTime: Long,
        val unexpectedExtras: Map<String, Any?>
    ) : EventPayload(EventPayloadType.SUSPICIOUS_INTENT, creationTime)
}
