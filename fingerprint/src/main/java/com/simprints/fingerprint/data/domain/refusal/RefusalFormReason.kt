package com.simprints.fingerprint.data.domain.refusal

import com.simprints.fingerprint.controllers.core.eventData.model.RefusalAnswer


enum class RefusalFormReason {
    SCANNER_NOT_WORKING,
    REFUSED,
    OTHER
}

fun RefusalFormReason.toRefusalAnswerForEvent(): RefusalAnswer =
    when(this){
        RefusalFormReason.SCANNER_NOT_WORKING -> RefusalAnswer.SCANNER_NOT_WORKING
        RefusalFormReason.REFUSED -> RefusalAnswer.BENEFICIARY_REFUSED
        RefusalFormReason.OTHER -> RefusalAnswer.OTHER
    }
