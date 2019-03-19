package com.simprints.id.data.analytics.eventdata.models.domain.events

import com.simprints.fingerprint.data.domain.refusal.RefusalFormReason

class RefusalEvent(val relativeStartTime: Long,
                   val relativeEndTime: Long,
                   val reason: Answer,
                   val otherText: String) : Event(EventType.REFUSAL) {

    enum class Answer {
        BENEFICIARY_REFUSED,
        SCANNER_NOT_WORKING,
        OTHER;

        companion object {
            fun fromRefusalReason(reasonForm: com.simprints.fingerprint.data.domain.refusal.RefusalFormReason): Answer {
                return when (reasonForm) {
                    com.simprints.fingerprint.data.domain.refusal.RefusalFormReason.SCANNER_NOT_WORKING -> SCANNER_NOT_WORKING
                    com.simprints.fingerprint.data.domain.refusal.RefusalFormReason.REFUSED -> BENEFICIARY_REFUSED
                    com.simprints.fingerprint.data.domain.refusal.RefusalFormReason.OTHER -> OTHER
                }
            }
        }
    }
}
