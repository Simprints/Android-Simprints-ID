package com.simprints.id.data.db.event.domain.models

import androidx.annotation.Keep
import com.simprints.id.data.db.event.domain.models.EventLabel.SessionIdLabel
import com.simprints.id.data.db.event.domain.models.EventType.INVALID_INTENT
import java.util.*

@Keep
class InvalidIntentEvent(
    val creationTime: Long,
    val action: String,
    val extras: Map<String, Any?>,
    sessionId: String = UUID.randomUUID().toString() //StopShip: to change in PAS-993
) : Event(
    UUID.randomUUID().toString(),
    mutableListOf(SessionIdLabel(sessionId)),
    InvalidIntentPayload(creationTime, DEFAULT_EVENT_VERSION, action, extras)) {


    @Keep
    class InvalidIntentPayload(createdAt: Long,
                               eventVersion: Int,
                               val action: String,
                               val extras: Map<String, Any?>) : EventPayload(INVALID_INTENT, eventVersion, createdAt)

}
