package com.simprints.id.data.exitform

import com.simprints.id.data.db.session.domain.models.events.RefusalEvent


enum class FingerprintExitFormReason {
    REFUSED_RELIGION,
    REFUSED_DATA_CONCERNS,
    REFUSED_PERMISSION,
    SCANNER_NOT_WORKING,
    REFUSED_NOT_PRESENT,
    REFUSED_YOUNG,
    OTHER
}

fun FingerprintExitFormReason.toRefusalEventAnswer(): RefusalEvent.Answer =
    when(this) {
        FingerprintExitFormReason.REFUSED_RELIGION -> RefusalEvent.Answer.REFUSED_RELIGION
        FingerprintExitFormReason.REFUSED_DATA_CONCERNS -> RefusalEvent.Answer.REFUSED_DATA_CONCERNS
        FingerprintExitFormReason.REFUSED_PERMISSION -> RefusalEvent.Answer.REFUSED_PERMISSION
        FingerprintExitFormReason.SCANNER_NOT_WORKING -> RefusalEvent.Answer.SCANNER_NOT_WORKING
        FingerprintExitFormReason.REFUSED_NOT_PRESENT -> RefusalEvent.Answer.REFUSED_NOT_PRESENT
        FingerprintExitFormReason.REFUSED_YOUNG -> RefusalEvent.Answer.REFUSED_YOUNG
        FingerprintExitFormReason.OTHER -> RefusalEvent.Answer.OTHER
    }
