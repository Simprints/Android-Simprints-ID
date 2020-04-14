package com.simprints.id.data.db.session.domain.models.events

import androidx.annotation.Keep

@Keep
class ConsentEvent(startTime: Long,
                   endTime: Long,
                   val consentType: Type,
                   var result: Result) : Event(EventType.CONSENT, startTime, endTime) {

    @Keep
    enum class Type {
        INDIVIDUAL, PARENTAL
    }

    @Keep
    enum class Result {
        ACCEPTED, DECLINED, NO_RESPONSE
    }
}
