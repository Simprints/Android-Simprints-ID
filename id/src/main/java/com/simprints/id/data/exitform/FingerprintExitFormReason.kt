package com.simprints.id.data.exitform

import com.simprints.id.data.analytics.eventdata.models.domain.events.RefusalEvent
import com.simprints.id.domain.moduleapi.app.responses.entities.RefusalFormReason

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

fun FingerprintExitFormReason.toAppRefusalFormReason(): RefusalFormReason =
    when (this) {
        FingerprintExitFormReason.REFUSED_RELIGION -> RefusalFormReason.REFUSED_RELIGION
        FingerprintExitFormReason.REFUSED_DATA_CONCERNS -> RefusalFormReason.REFUSED_DATA_CONCERNS
        FingerprintExitFormReason.REFUSED_PERMISSION -> RefusalFormReason.REFUSED_PERMISSION
        FingerprintExitFormReason.SCANNER_NOT_WORKING -> RefusalFormReason.SCANNER_NOT_WORKING
        FingerprintExitFormReason.REFUSED_NOT_PRESENT -> RefusalFormReason.REFUSED_NOT_PRESENT
        FingerprintExitFormReason.REFUSED_YOUNG -> RefusalFormReason.REFUSED_YOUNG
        FingerprintExitFormReason.OTHER -> RefusalFormReason.OTHER
    }
