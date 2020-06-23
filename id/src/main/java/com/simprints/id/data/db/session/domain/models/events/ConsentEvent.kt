package com.simprints.id.data.db.session.domain.models.events

import androidx.annotation.Keep
import java.util.*

@Keep
class ConsentEvent(
    startTime: Long,
    endTime: Long,
    consentType: ConsentPayload.Type,
    result: ConsentPayload.Result,
    sessionId: String = UUID.randomUUID().toString() //StopShip: to change in PAS-993
) : Event(
    UUID.randomUUID().toString(),
    listOf(EventLabel.SessionId(sessionId)),
    ConsentPayload(startTime, endTime, consentType, result)) {


    @Keep
    class ConsentPayload(val startTime: Long,
                         val endTime: Long,
                         val consentType: Type,
                         var result: Result) : EventPayload(EventPayloadType.CONSENT) {

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
