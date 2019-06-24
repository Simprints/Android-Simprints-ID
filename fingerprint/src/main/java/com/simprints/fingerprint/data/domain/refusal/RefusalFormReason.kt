package com.simprints.fingerprint.data.domain.refusal

import com.simprints.fingerprint.controllers.core.eventData.model.RefusalAnswer


enum class RefusalFormReason {
    REFUSED_RELIGION,
    REFUSED_DATA_CONCERNS,
    REFUSED_PERMISSION,
    SCANNER_NOT_WORKING,
    REFUSED_YOUNG,
    REFUSED_SICK,
    REFUSED_PREGNANT,
    OTHER
}

fun RefusalFormReason.toRefusalAnswerForEvent(): RefusalAnswer =
    when(this){
        RefusalFormReason.SCANNER_NOT_WORKING -> RefusalAnswer.SCANNER_NOT_WORKING
        RefusalFormReason.OTHER -> RefusalAnswer.OTHER
        else -> RefusalAnswer.BENEFICIARY_REFUSED
    }
