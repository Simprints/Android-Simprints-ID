package com.simprints.id.data.db.event.domain.events

import androidx.annotation.Keep
import java.util.*

@Keep
class SuspiciousIntentEvent(
    createdAt: Long,
    unexpectedExtras: Map<String, Any?>,
    sessionId: String = UUID.randomUUID().toString() //StopShip: to change in PAS-993
) : Event(
    UUID.randomUUID().toString(),
    mutableListOf(EventLabel.SessionId(sessionId)),
    SuspiciousIntentPayload(createdAt, DEFAULT_EVENT_VERSION, unexpectedExtras)) {

    @Keep
    class SuspiciousIntentPayload(
        createdAt: Long,
        eventVersion: Int,
        val unexpectedExtras: Map<String, Any?>
    ) : EventPayload(EventPayloadType.SUSPICIOUS_INTENT, eventVersion, createdAt)
}
