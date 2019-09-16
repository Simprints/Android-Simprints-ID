package com.simprints.id.data.exitform

import com.simprints.id.data.analytics.eventdata.models.domain.events.RefusalEvent

enum class ExitFormReason {
    REFUSED_RELIGION,
    REFUSED_DATA_CONCERNS,
    REFUSED_PERMISSION,
    SCANNER_NOT_WORKING,
    REFUSED_NOT_PRESENT,
    REFUSED_YOUNG,
    OTHER
}

fun ExitFormReason.toRefusalEventAnswer(): RefusalEvent.Answer =
    when(this) {
        ExitFormReason.REFUSED_RELIGION -> RefusalEvent.Answer.REFUSED_RELIGION
        ExitFormReason.REFUSED_DATA_CONCERNS -> RefusalEvent.Answer.REFUSED_DATA_CONCERNS
        ExitFormReason.REFUSED_PERMISSION -> RefusalEvent.Answer.REFUSED_PERMISSION
        ExitFormReason.SCANNER_NOT_WORKING -> RefusalEvent.Answer.SCANNER_NOT_WORKING
        ExitFormReason.REFUSED_NOT_PRESENT -> RefusalEvent.Answer.REFUSED_NOT_PRESENT
        ExitFormReason.REFUSED_YOUNG -> RefusalEvent.Answer.REFUSED_YOUNG
        ExitFormReason.OTHER -> RefusalEvent.Answer.OTHER
    }
