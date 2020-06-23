package com.simprints.id.data.db.session.domain.models.events.callback

import androidx.annotation.Keep
import com.simprints.id.data.db.session.domain.models.events.Event
import com.simprints.id.data.db.session.domain.models.events.EventLabel
import com.simprints.id.data.db.session.domain.models.events.EventPayload
import com.simprints.id.data.db.session.domain.models.events.EventPayloadType
import java.util.*

@Keep
class VerificationCallbackEvent(
    startTime: Long,
    score: CallbackComparisonScore,
    sessionId: String = UUID.randomUUID().toString() //StopShip: to change in PAS-993
) : Event(
    UUID.randomUUID().toString(),
    listOf(EventLabel.SessionId(sessionId)),
    VerificationCallbackPayload(startTime, score)) {

    @Keep
    class VerificationCallbackPayload(
        val startTime: Long,
        val score: CallbackComparisonScore
    ) : EventPayload(EventPayloadType.CALLBACK_VERIFICATION)
}
