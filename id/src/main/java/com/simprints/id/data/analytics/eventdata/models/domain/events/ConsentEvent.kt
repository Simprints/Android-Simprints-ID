package com.simprints.id.data.analytics.eventdata.models.domain.events

import androidx.annotation.Keep

@Keep
class ConsentEvent(starTime: Long,
                   endTime: Long,
                   val consentType: Type,
                   var result: Result) : Event(EventType.CONSENT, starTime, endTime) {

    @Keep
    enum class Type {
        INDIVIDUAL, PARENTAL
    }

    @Keep
    enum class Result {
        ACCEPTED, DECLINED, NO_RESPONSE
    }
}
