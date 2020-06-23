package com.simprints.id.data.db.session.domain.models.events

import androidx.annotation.Keep
import com.simprints.id.data.db.session.domain.models.events.EventPayloadType.INVALID_INTENT
import java.util.*

@Keep
class InvalidIntentEvent(
    val startTime: Long,
    val action: String,
    val extras: Map<String, Any?>,
    sessionId: String = UUID.randomUUID().toString() //StopShip: to change in PAS-993
) : Event(
    UUID.randomUUID().toString(),
    listOf(EventLabel.SessionId(sessionId)),
    InvalidIntentPayload(startTime, action, extras)) {


    @Keep
    class InvalidIntentPayload(val startTime: Long,
                               val action: String,
                               val extras: Map<String, Any?>) : EventPayload(INVALID_INTENT)

}
