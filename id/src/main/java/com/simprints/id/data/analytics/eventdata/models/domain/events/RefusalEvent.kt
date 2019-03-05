package com.simprints.id.data.analytics.eventdata.models.domain.events

import com.simprints.id.data.analytics.eventdata.models.domain.EventType
import com.simprints.id.domain.refusal_form.RefusalFormReason

class RefusalEvent(val relativeStartTime: Long,
                   val relativeEndTime: Long,
                   val reason: Answer,
                   val otherText: String) : Event(EventType.REFUSAL) {

    enum class Answer {
        BENEFICIARY_REFUSED,
        SCANNER_NOT_WORKING,
        OTHER;

        companion object {
            fun fromRefusalReason(reasonForm: RefusalFormReason): Answer {
                return when (reasonForm) {
                    RefusalFormReason.SCANNER_NOT_WORKING -> SCANNER_NOT_WORKING
                    RefusalFormReason.REFUSED -> BENEFICIARY_REFUSED
                    RefusalFormReason.OTHER -> OTHER
                }
            }
        }
    }
}
