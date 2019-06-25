package com.simprints.fingerprint.data.domain.refusal

import com.simprints.fingerprint.controllers.core.eventData.model.RefusalAnswer
import com.simprints.id.domain.moduleapi.fingerprint.responses.entities.FingerprintRefusalFormReason


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

fun RefusalFormReason.toFingerprintRefusalFormReason(): FingerprintRefusalFormReason =
    when(this) {
        RefusalFormReason.REFUSED_RELIGION -> FingerprintRefusalFormReason.REFUSED_RELIGION
        RefusalFormReason.REFUSED_DATA_CONCERNS -> FingerprintRefusalFormReason.REFUSED_DATA_CONCERNS
        RefusalFormReason.REFUSED_PERMISSION -> FingerprintRefusalFormReason.REFUSED_PERMISSION
        RefusalFormReason.SCANNER_NOT_WORKING -> FingerprintRefusalFormReason.SCANNER_NOT_WORKING
        RefusalFormReason.REFUSED_YOUNG -> FingerprintRefusalFormReason.REFUSED_YOUNG
        RefusalFormReason.REFUSED_SICK -> FingerprintRefusalFormReason.REFUSED_SICK
        RefusalFormReason.REFUSED_PREGNANT -> FingerprintRefusalFormReason.REFUSED_SICK
        RefusalFormReason.OTHER -> FingerprintRefusalFormReason.OTHER
    }

