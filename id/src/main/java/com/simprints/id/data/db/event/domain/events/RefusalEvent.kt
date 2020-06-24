package com.simprints.id.data.db.event.domain.events

import androidx.annotation.Keep
import java.util.*

@Keep
class RefusalEvent(
    startTime: Long,
    endTime: Long,
    reason: RefusalPayload.Answer,
    otherText: String,
    sessionId: String = UUID.randomUUID().toString(), //StopShip: to change in PAS-993
    sessionStartTime: Long = 0 //StopShip: to change in PAS-993
) : Event(
    UUID.randomUUID().toString(),
    listOf(EventLabel.SessionId(sessionId)),
    RefusalPayload(startTime, startTime - sessionStartTime, endTime, endTime - sessionStartTime, reason, otherText)) {


    @Keep
    class RefusalPayload(startTime: Long,
                         relativeStartTime: Long,
                         val endTime: Long,
                         val relativeEndTime: Long,
                         val reason: Answer,
                         val otherText: String) : EventPayload(EventPayloadType.REFUSAL, startTime, relativeStartTime) {

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
