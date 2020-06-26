package com.simprints.id.data.db.event.domain.events.callback

import androidx.annotation.Keep
import com.simprints.id.data.db.event.domain.events.Event
import com.simprints.id.data.db.event.domain.events.EventLabel
import com.simprints.id.data.db.event.domain.events.EventPayload
import com.simprints.id.data.db.event.domain.events.EventPayloadType
import java.util.*

@Keep
class RefusalCallbackEvent(
    creationTime: Long,
    reason: String,
    extra: String,
    sessionId: String = UUID.randomUUID().toString() //StopShip: to change in PAS-993
) : Event(
    UUID.randomUUID().toString(),
    listOf(EventLabel.SessionId(sessionId)),
    RefusalCallbackPayload(creationTime, reason, extra)) {

    @Keep
    class RefusalCallbackPayload(startTime: Long,
                                 val reason: String,
                                 val extra: String) : EventPayload(EventPayloadType.CALLBACK_REFUSAL, startTime)

}
