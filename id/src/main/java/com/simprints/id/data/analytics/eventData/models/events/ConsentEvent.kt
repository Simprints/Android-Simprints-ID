package com.simprints.id.data.analytics.eventData.models.events

import com.simprints.id.data.analytics.eventData.models.EventType

class ConsentEvent(val relativeStartTime: Long,
                   var relativeEndTime: Long,
                   val consentType: Type,
                   var result: Result) : Event(EventType.CONSENT) {

    enum class Type {
        INDIVIDUAL, PARENTAL
    }
    enum class Result {
        ACCEPTED, DECLINED, NO_RESPONSE
    }
}
