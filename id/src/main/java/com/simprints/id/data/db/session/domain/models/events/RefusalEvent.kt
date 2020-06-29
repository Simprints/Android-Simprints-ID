package com.simprints.id.data.db.session.domain.models.events

import androidx.annotation.Keep

@Keep
class RefusalEvent(startTime: Long,
                   endTime: Long,
                   val reason: Answer,
                   val otherText: String) : Event(EventType.REFUSAL, startTime, endTime) {

    @Keep
    enum class Answer {
        REFUSED_RELIGION,
        REFUSED_DATA_CONCERNS,
        REFUSED_PERMISSION,
        SCANNER_NOT_WORKING,
        APP_NOT_WORKING,
        REFUSED_NOT_PRESENT,
        REFUSED_YOUNG,
        OTHER
    }
}
