package com.simprints.id.data.db.event.domain.models

import androidx.annotation.Keep
import com.simprints.id.data.db.event.domain.models.EventLabel.SessionIdLabel
import java.util.*

@Keep
class RefusalEvent(
    createdAt: Long,
    endTime: Long,
    reason: RefusalPayload.Answer,
    otherText: String,
    sessionId: String = UUID.randomUUID().toString() //StopShip: to change in PAS-993
) : Event(
    UUID.randomUUID().toString(),
    mutableListOf(SessionIdLabel(sessionId)),
    RefusalPayload(createdAt, DEFAULT_EVENT_VERSION, endTime, reason, otherText)) {


    @Keep
    class RefusalPayload(createdAt: Long,
                         eventVersion: Int,
                         val endTime: Long,
                         val reason: Answer,
                         val otherText: String) : EventPayload(EventType.REFUSAL, eventVersion, createdAt) {

        @Keep
        enum class Answer {
            REFUSED_RELIGION,
            REFUSED_DATA_CONCERNS,
            REFUSED_PERMISSION,
            SCANNER_NOT_WORKING,
            REFUSED_NOT_PRESENT,
            REFUSED_YOUNG,
            OTHER
        }
    }
}
