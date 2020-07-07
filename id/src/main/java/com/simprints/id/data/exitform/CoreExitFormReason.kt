package com.simprints.id.data.exitform

import com.simprints.id.data.db.event.domain.events.RefusalEvent.RefusalPayload.Answer

enum class CoreExitFormReason {
    REFUSED_RELIGION,
    REFUSED_DATA_CONCERNS,
    REFUSED_PERMISSION,
    SCANNER_NOT_WORKING,
    REFUSED_NOT_PRESENT,
    REFUSED_YOUNG,
    OTHER
}

fun CoreExitFormReason.toRefusalEventAnswer(): Answer =
    when(this) {
        CoreExitFormReason.REFUSED_RELIGION -> Answer.REFUSED_RELIGION
        CoreExitFormReason.REFUSED_DATA_CONCERNS -> Answer.REFUSED_DATA_CONCERNS
        CoreExitFormReason.REFUSED_PERMISSION -> Answer.REFUSED_PERMISSION
        CoreExitFormReason.SCANNER_NOT_WORKING -> Answer.SCANNER_NOT_WORKING
        CoreExitFormReason.REFUSED_NOT_PRESENT -> Answer.REFUSED_NOT_PRESENT
        CoreExitFormReason.REFUSED_YOUNG -> Answer.REFUSED_YOUNG
        CoreExitFormReason.OTHER -> Answer.OTHER
    }
