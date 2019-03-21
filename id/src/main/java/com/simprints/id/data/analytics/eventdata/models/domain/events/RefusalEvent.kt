package com.simprints.id.data.analytics.eventdata.models.domain.events

class RefusalEvent(val relativeStartTime: Long,
                   val relativeEndTime: Long,
                   val reason: Answer,
                   val otherText: String) : Event(EventType.REFUSAL) {

    enum class Answer {
        BENEFICIARY_REFUSED,
        SCANNER_NOT_WORKING,
        OTHER;
    }
}
