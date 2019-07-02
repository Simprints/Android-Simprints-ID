package com.simprints.id.data.analytics.eventdata.models.domain.events

import androidx.annotation.Keep

@Keep
class RefusalEvent(starTime: Long,
                   endTime: Long,
                   val reason: Answer,
                   val otherText: String) : Event(EventType.REFUSAL, starTime, endTime) {

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
