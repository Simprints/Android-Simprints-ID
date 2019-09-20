package com.simprints.id.domain.moduleapi.app.responses.entities

import com.simprints.id.data.exitform.ExitFormReason

enum class RefusalFormReason {
    REFUSED_RELIGION,
    REFUSED_DATA_CONCERNS,
    REFUSED_PERMISSION,
    SCANNER_NOT_WORKING,
    REFUSED_NOT_PRESENT,
    REFUSED_YOUNG,
    OTHER
}

fun ExitFormReason.fromDomainToModuleApi(): RefusalFormReason =
    when (this) {
        ExitFormReason.REFUSED_RELIGION -> RefusalFormReason.REFUSED_RELIGION
        ExitFormReason.REFUSED_DATA_CONCERNS -> RefusalFormReason.REFUSED_DATA_CONCERNS
        ExitFormReason.REFUSED_PERMISSION -> RefusalFormReason.REFUSED_PERMISSION
        ExitFormReason.SCANNER_NOT_WORKING -> RefusalFormReason.SCANNER_NOT_WORKING
        ExitFormReason.REFUSED_NOT_PRESENT -> RefusalFormReason.REFUSED_NOT_PRESENT
        ExitFormReason.REFUSED_YOUNG -> RefusalFormReason.REFUSED_YOUNG
        ExitFormReason.OTHER -> RefusalFormReason.OTHER
    }
