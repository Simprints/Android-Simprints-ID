package com.simprints.id.data.exitform

import com.simprints.id.data.db.event.domain.models.RefusalEvent.RefusalPayload.Answer

enum class FingerprintExitFormReason {
    REFUSED_RELIGION,
    REFUSED_DATA_CONCERNS,
    REFUSED_PERMISSION,
    SCANNER_NOT_WORKING,
    REFUSED_NOT_PRESENT,
    REFUSED_YOUNG,
    OTHER
}

fun FingerprintExitFormReason.toRefusalEventAnswer(): Answer =
    when(this) {
        FingerprintExitFormReason.REFUSED_RELIGION -> Answer.REFUSED_RELIGION
        FingerprintExitFormReason.REFUSED_DATA_CONCERNS -> Answer.REFUSED_DATA_CONCERNS
        FingerprintExitFormReason.REFUSED_PERMISSION -> Answer.REFUSED_PERMISSION
        FingerprintExitFormReason.SCANNER_NOT_WORKING -> Answer.SCANNER_NOT_WORKING
        FingerprintExitFormReason.REFUSED_NOT_PRESENT -> Answer.REFUSED_NOT_PRESENT
        FingerprintExitFormReason.REFUSED_YOUNG -> Answer.REFUSED_YOUNG
        FingerprintExitFormReason.OTHER -> Answer.OTHER
    }
