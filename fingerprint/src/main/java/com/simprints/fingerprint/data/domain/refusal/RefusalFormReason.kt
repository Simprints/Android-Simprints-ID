package com.simprints.fingerprint.data.domain.refusal

import com.simprints.id.data.analytics.eventdata.models.domain.events.RefusalEvent

enum class RefusalFormReason {
    SCANNER_NOT_WORKING,
    REFUSED,
    OTHER
}

fun RefusalFormReason.toAnswerEvent(): RefusalEvent.Answer =
    when(this){
        RefusalFormReason.SCANNER_NOT_WORKING -> RefusalEvent.Answer.SCANNER_NOT_WORKING
        RefusalFormReason.REFUSED -> RefusalEvent.Answer.BENEFICIARY_REFUSED
        RefusalFormReason.OTHER -> RefusalEvent.Answer.OTHER
    }
