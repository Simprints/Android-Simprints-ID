package com.simprints.id.data.db.event.domain.models

import androidx.annotation.Keep
import com.simprints.id.data.db.event.domain.models.EventLabel.SessionIdLabel
import com.simprints.id.data.db.event.domain.models.EventType.SUSPICIOUS_INTENT
import java.util.*

@Keep
class SuspiciousIntentEvent(
    createdAt: Long,
    unexpectedExtras: Map<String, Any?>,
    sessionId: String = UUID.randomUUID().toString() //StopShip: to change in PAS-993
) : Event(
    UUID.randomUUID().toString(),
    mutableListOf(SessionIdLabel(sessionId)),
    SuspiciousIntentPayload(createdAt, EVENT_VERSION, unexpectedExtras),
    SUSPICIOUS_INTENT) {

    @Keep
    class SuspiciousIntentPayload(
        createdAt: Long,
        eventVersion: Int,
        val unexpectedExtras: Map<String, Any?>
    ) : EventPayload(SUSPICIOUS_INTENT, eventVersion, createdAt)

    companion object {
        const val EVENT_VERSION = DEFAULT_EVENT_VERSION
    }
}
