package com.simprints.id.data.db.event.domain.events

import androidx.annotation.Keep
import java.util.*

@Keep
class RefusalEvent(
    creationTime: Long,
    endTime: Long,
    reason: RefusalPayload.Answer,
    otherText: String,
    sessionId: String = UUID.randomUUID().toString() //StopShip: to change in PAS-993
) : Event(
    UUID.randomUUID().toString(),
    DEFAULT_EVENT_VERSION,
    listOf(EventLabel.SessionId(sessionId)),
    RefusalPayload(creationTime, DEFAULT_EVENT_VERSION, endTime, reason, otherText)) {


    @Keep
    class RefusalPayload(creationTime: Long,
                         version: Int,
                         val endTime: Long,
                         val reason: Answer,
                         val otherText: String) : EventPayload(EventPayloadType.REFUSAL, version, creationTime) {

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
