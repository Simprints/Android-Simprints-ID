package com.simprints.id.data.analytics.eventdata.models.domain.events

import com.simprints.id.data.analytics.eventdata.models.domain.EventType
import com.simprints.id.data.db.remote.enums.REFUSAL_FORM_REASON

class RefusalEvent(val relativeStartTime: Long,
                   val relativeEndTime: Long,
                   val reason: Answer,
                   val otherText: String) : Event(EventType.REFUSAL) {

    enum class Answer {
        BENEFICIARY_REFUSED,
        SCANNER_NOT_WORKING,
        OTHER;

        companion object {
            fun fromRefusalReason(reasonForm: REFUSAL_FORM_REASON): Answer {
                return when (reasonForm) {
                    REFUSAL_FORM_REASON.SCANNER_NOT_WORKING -> SCANNER_NOT_WORKING
                    REFUSAL_FORM_REASON.REFUSED -> BENEFICIARY_REFUSED
                    REFUSAL_FORM_REASON.OTHER -> OTHER
                }
            }
        }
    }
}
