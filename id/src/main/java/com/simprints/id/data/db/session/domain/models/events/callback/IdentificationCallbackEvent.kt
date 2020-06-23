package com.simprints.id.data.db.session.domain.models.events.callback

import androidx.annotation.Keep
import com.simprints.id.data.db.session.domain.models.events.Event
import com.simprints.id.data.db.session.domain.models.events.EventLabel
import com.simprints.id.data.db.session.domain.models.events.EventPayload
import com.simprints.id.data.db.session.domain.models.events.EventPayloadType
import java.util.*

@Keep
class IdentificationCallbackEvent(
    startTime: Long,
    sessionId: String,
    scores: List<CallbackComparisonScore>
) : Event(
    UUID.randomUUID().toString(),
    listOf(EventLabel.SessionId(sessionId)),
    IdentificationCallbackPayload(startTime, sessionId, scores)) {

    @Keep
    class IdentificationCallbackPayload(
        val startTime: Long,
        val sessionId: String,
        val scores: List<CallbackComparisonScore>
    ) : EventPayload(EventPayloadType.CALLBACK_IDENTIFICATION)

}
