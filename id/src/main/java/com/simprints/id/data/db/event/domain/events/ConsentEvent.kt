package com.simprints.id.data.db.event.domain.events

import androidx.annotation.Keep
import java.util.*

@Keep
class ConsentEvent(
    creationTime: Long,
    endTime: Long,
    consentType: ConsentPayload.Type,
    result: ConsentPayload.Result,
    sessionId: String = UUID.randomUUID().toString() //StopShip: to change in PAS-993
) : Event(
    UUID.randomUUID().toString(),
    listOf(EventLabel.SessionId(sessionId)),
    ConsentPayload(creationTime, endTime, consentType, result)) {


    @Keep
    class ConsentPayload(creationTime: Long,
                         val endTime: Long,
                         val consentType: Type,
                         var result: Result) : EventPayload(EventPayloadType.CONSENT, creationTime) {

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
