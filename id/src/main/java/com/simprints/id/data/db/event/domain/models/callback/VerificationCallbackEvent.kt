package com.simprints.id.data.db.event.domain.models.callback

import androidx.annotation.Keep
import com.simprints.id.data.db.event.domain.models.Event
import com.simprints.id.data.db.event.domain.models.EventLabel.SessionIdLabel
import com.simprints.id.data.db.event.domain.models.EventPayload
import com.simprints.id.data.db.event.domain.models.EventType.CALLBACK_VERIFICATION
import java.util.*

@Keep
class VerificationCallbackEvent(
    createdAt: Long,
    score: CallbackComparisonScore,
    sessionId: String = UUID.randomUUID().toString() //StopShip: to change in PAS-993
) : Event(
    UUID.randomUUID().toString(),
    mutableListOf(SessionIdLabel(sessionId)),
    VerificationCallbackPayload(createdAt, EVENT_VERSION, score),
    CALLBACK_VERIFICATION) {

    @Keep
    class VerificationCallbackPayload(
        createdAt: Long,
        eventVersion: Int,
        val score: CallbackComparisonScore
    ) : EventPayload(CALLBACK_VERIFICATION, eventVersion, createdAt)

    companion object {
        const val EVENT_VERSION = DEFAULT_EVENT_VERSION
    }
}
