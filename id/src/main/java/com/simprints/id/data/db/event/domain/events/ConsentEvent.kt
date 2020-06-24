package com.simprints.id.data.db.event.domain.events

import androidx.annotation.Keep
import java.util.*

@Keep
class ConsentEvent(
    startTime: Long,
    endTime: Long,
    consentType: ConsentPayload.Type,
    result: ConsentPayload.Result,
    sessionId: String = UUID.randomUUID().toString(), //StopShip: to change in PAS-993
    sessionStartTime: Long = 0 //StopShip: to change in PAS-993
) : Event(
    UUID.randomUUID().toString(),
    listOf(EventLabel.SessionId(sessionId)),
    ConsentPayload(startTime, startTime - sessionStartTime, endTime, endTime - sessionStartTime, consentType, result)) {


    @Keep
    class ConsentPayload(startTime: Long,
                         relativeStartTime: Long,
                         val endTime: Long,
                         val relativeEndTime: Long,
                         val consentType: Type,
                         var result: Result) : EventPayload(EventPayloadType.CONSENT, startTime, relativeStartTime) {

        @Keep
        enum class Type {
            INDIVIDUAL, PARENTAL
        }

        @Keep
        enum class Result {
            ACCEPTED, DECLINED, NO_RESPONSE
        }
    }
}
