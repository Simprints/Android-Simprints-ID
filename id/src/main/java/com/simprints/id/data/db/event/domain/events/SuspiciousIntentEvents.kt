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
    DEFAULT_EVENT_VERSION,
    listOf(EventLabel.SessionId(sessionId)),
    SuspiciousIntentPayload(creationTime, DEFAULT_EVENT_VERSION, unexpectedExtras)) {

    @Keep
    class SuspiciousIntentPayload(
        creationTime: Long,
        version: Int,
        val unexpectedExtras: Map<String, Any?>
    ) : EventPayload(EventPayloadType.SUSPICIOUS_INTENT, version, creationTime)
}
