package com.simprints.id.data.analytics.events.models

class ConsentEvent(val relativeStartTime: Long,
                   var relativeEndTime: Long,
                   val consentType: Type,
                   var consent: Result): Event(EventType.CONSENT) {

    enum class Type {
        INDIVIDUAL, PARENTAL
    }
    enum class Result {
        ACCEPTED, DECLINED, NO_RESPONSE
    }
}
