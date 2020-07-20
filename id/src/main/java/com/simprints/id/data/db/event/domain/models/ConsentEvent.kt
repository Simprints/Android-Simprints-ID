package com.simprints.id.data.db.event.domain.models

import androidx.annotation.Keep
import com.simprints.id.data.db.event.domain.models.EventLabel.SessionIdLabel
import java.util.*

@Keep
class ConsentEvent(
    createdAt: Long,
    endTime: Long,
    consentType: ConsentPayload.Type,
    result: ConsentPayload.Result,
    sessionId: String = UUID.randomUUID().toString() //StopShip: to change in PAS-993
) : Event(
    UUID.randomUUID().toString(),
    mutableListOf(SessionIdLabel(sessionId)),
    ConsentPayload(createdAt, DEFAULT_EVENT_VERSION, endTime, consentType, result)) {


    @Keep
    class ConsentPayload(createdAt: Long,
                         eventVersion: Int,
                         val endTime: Long,
                         val consentType: Type,
                         var result: Result) : EventPayload(EventType.CONSENT, eventVersion, createdAt) {

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
