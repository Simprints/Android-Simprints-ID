package com.simprints.id.data.db.event.domain.models

import androidx.annotation.Keep
import com.simprints.id.data.db.event.domain.models.EventLabel.SessionIdLabel
import com.simprints.id.data.db.event.domain.models.EventType.CONSENT
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
    ConsentPayload(createdAt, EVENT_VERSION, endTime, consentType, result),
    CONSENT) {


    @Keep
    class ConsentPayload(createdAt: Long,
                         eventVersion: Int,
                         endTimeAt: Long,
                         val consentType: Type,
                         var result: Result) : EventPayload(CONSENT, eventVersion, createdAt, endTimeAt) {

        @Keep
        enum class Type {
            INDIVIDUAL, PARENTAL
        }

        @Keep
        enum class Result {
            ACCEPTED, DECLINED, NO_RESPONSE
        }
    }

    companion object {
        const val EVENT_VERSION = DEFAULT_EVENT_VERSION
    }
}
