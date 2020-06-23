package com.simprints.id.data.db.session.domain.models.events.callback

import androidx.annotation.Keep
import com.simprints.id.data.db.session.domain.models.events.Event
import com.simprints.id.data.db.session.domain.models.events.EventLabel
import com.simprints.id.data.db.session.domain.models.events.EventPayload
import com.simprints.id.data.db.session.domain.models.events.EventPayloadType
import java.util.*

@Keep
class RefusalCallbackEvent(
    startTime: Long,
    reason: String,
    extra: String,
    sessionId: String = UUID.randomUUID().toString() //StopShip: to change in PAS-993
) : Event(
    UUID.randomUUID().toString(),
    listOf(EventLabel.SessionId(sessionId)),
    RefusalCallbackPayload(startTime, reason, extra)) {

    @Keep
    class RefusalCallbackPayload(val startTime: Long,
                                 val reason: String,
                                 val extra: String) : EventPayload(EventPayloadType.CALLBACK_REFUSAL)

}
