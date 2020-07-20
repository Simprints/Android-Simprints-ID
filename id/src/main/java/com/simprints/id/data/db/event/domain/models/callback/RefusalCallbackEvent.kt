package com.simprints.id.data.db.event.domain.models.callback

import androidx.annotation.Keep
import com.simprints.id.data.db.event.domain.models.Event
import com.simprints.id.data.db.event.domain.models.EventLabel.SessionIdLabel
import com.simprints.id.data.db.event.domain.models.EventPayload
import com.simprints.id.data.db.event.domain.models.EventType
import java.util.*

@Keep
class RefusalCallbackEvent(
    createdAt: Long,
    reason: String,
    extra: String,
    sessionId: String = UUID.randomUUID().toString() //StopShip: to change in PAS-993
) : Event(
    UUID.randomUUID().toString(),
    mutableListOf(SessionIdLabel(sessionId)),
    RefusalCallbackPayload(createdAt, DEFAULT_EVENT_VERSION, reason, extra)) {

    @Keep
    class RefusalCallbackPayload(createdAt: Long,
                                 eventVersion: Int,
                                 val reason: String,
                                 val extra: String) : EventPayload(EventType.CALLBACK_REFUSAL, eventVersion, createdAt)

}
