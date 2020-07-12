package com.simprints.id.data.db.event.domain.events.callback

import androidx.annotation.Keep
import com.simprints.id.data.db.event.domain.events.Event
import com.simprints.id.data.db.event.domain.events.EventPayload
import com.simprints.id.data.db.event.domain.events.EventPayloadType
import java.util.*

@Keep
class RefusalCallbackEvent(
    createdAt: Long,
    reason: String,
    extra: String,
    sessionId: String = UUID.randomUUID().toString() //StopShip: to change in PAS-993
) : Event(
    UUID.randomUUID().toString(),
    DEFAULT_EVENT_VERSION,
    listOf(EventLabel.SessionId(sessionId)),
    RefusalCallbackPayload(createdAt, DEFAULT_EVENT_VERSION, reason, extra)) {

    @Keep
    class RefusalCallbackPayload(createdAt: Long,
                                 eventVersion: Int,
                                 val reason: String,
                                 val extra: String) : EventPayload(EventPayloadType.CALLBACK_REFUSAL, eventVersion, createdAt)

}
