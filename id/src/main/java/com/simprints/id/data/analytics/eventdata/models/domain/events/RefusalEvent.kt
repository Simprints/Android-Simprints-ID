package com.simprints.id.data.analytics.eventdata.models.domain.events

import androidx.annotation.Keep

@Keep
class RefusalEvent(override val starTime: Long,
                   override val endTime: Long,
                   val reason: Answer,
                   val otherText: String) : Event(EventType.REFUSAL) {

    @Keep
    enum class Answer {
        BENEFICIARY_REFUSED,
        SCANNER_NOT_WORKING,
        OTHER;
    }
}
