package com.simprints.id.data.db.session.domain.models.events

import androidx.annotation.Keep
import java.util.*

@Keep
class SuspiciousIntentEvent(
    startTime: Long,
    unexpectedExtras: Map<String, Any?>,
    sessionId: String = UUID.randomUUID().toString() //StopShip: to change in PAS-993
) : Event(
    UUID.randomUUID().toString(),
    listOf(EventLabel.SessionId(sessionId)),
    SuspiciousIntentPayload(startTime, unexpectedExtras)) {

    @Keep
    class SuspiciousIntentPayload(
        val startTime: Long,
        val unexpectedExtras: Map<String, Any?>
    ) : EventPayload(EventPayloadType.SUSPICIOUS_INTENT)
}
