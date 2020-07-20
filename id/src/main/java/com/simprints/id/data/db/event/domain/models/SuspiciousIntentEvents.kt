package com.simprints.id.data.db.event.domain.models

import androidx.annotation.Keep
import com.simprints.id.data.db.event.domain.models.EventLabel.SessionIdLabel
import java.util.*

@Keep
class SuspiciousIntentEvent(
    createdAt: Long,
    unexpectedExtras: Map<String, Any?>,
    sessionId: String = UUID.randomUUID().toString() //StopShip: to change in PAS-993
) : Event(
    UUID.randomUUID().toString(),
    mutableListOf(SessionIdLabel(sessionId)),
    SuspiciousIntentPayload(createdAt, DEFAULT_EVENT_VERSION, unexpectedExtras)) {

    @Keep
    class SuspiciousIntentPayload(
        createdAt: Long,
        eventVersion: Int,
        val unexpectedExtras: Map<String, Any?>
    ) : EventPayload(EventType.SUSPICIOUS_INTENT, eventVersion, createdAt)
}
