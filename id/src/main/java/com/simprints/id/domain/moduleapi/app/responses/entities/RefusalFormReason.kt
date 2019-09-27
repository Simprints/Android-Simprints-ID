package com.simprints.id.domain.moduleapi.app.responses.entities

import com.simprints.id.data.exitform.CoreExitFormReason
import com.simprints.id.data.exitform.FaceExitFormReason
import com.simprints.id.data.exitform.FingerprintExitFormReason

enum class RefusalFormReason {
    REFUSED_RELIGION,
    REFUSED_DATA_CONCERNS,
    REFUSED_PERMISSION,
    SCANNER_NOT_WORKING,
    REFUSED_NOT_PRESENT,
    REFUSED_YOUNG,
    OTHER
}

fun CoreExitFormReason.fromDomainToModuleApi(): RefusalFormReason =
    when (this) {
        CoreExitFormReason.REFUSED_RELIGION -> RefusalFormReason.REFUSED_RELIGION
        CoreExitFormReason.REFUSED_DATA_CONCERNS -> RefusalFormReason.REFUSED_DATA_CONCERNS
        CoreExitFormReason.REFUSED_PERMISSION -> RefusalFormReason.REFUSED_PERMISSION
        CoreExitFormReason.SCANNER_NOT_WORKING -> RefusalFormReason.SCANNER_NOT_WORKING
        CoreExitFormReason.REFUSED_NOT_PRESENT -> RefusalFormReason.REFUSED_NOT_PRESENT
        CoreExitFormReason.REFUSED_YOUNG -> RefusalFormReason.REFUSED_YOUNG
        CoreExitFormReason.OTHER -> RefusalFormReason.OTHER
    }

fun FingerprintExitFormReason.fromDomainToModuleApi(): RefusalFormReason =
    when (this) {
        FingerprintExitFormReason.REFUSED_RELIGION -> RefusalFormReason.REFUSED_RELIGION
        FingerprintExitFormReason.REFUSED_DATA_CONCERNS -> RefusalFormReason.REFUSED_DATA_CONCERNS
        FingerprintExitFormReason.REFUSED_PERMISSION -> RefusalFormReason.REFUSED_PERMISSION
        FingerprintExitFormReason.SCANNER_NOT_WORKING -> RefusalFormReason.SCANNER_NOT_WORKING
        FingerprintExitFormReason.REFUSED_NOT_PRESENT -> RefusalFormReason.REFUSED_NOT_PRESENT
        FingerprintExitFormReason.REFUSED_YOUNG -> RefusalFormReason.REFUSED_YOUNG
        FingerprintExitFormReason.OTHER -> RefusalFormReason.OTHER
    }

fun FaceExitFormReason.fromDomainToModuleApi(): RefusalFormReason =
    when (this) {
        FaceExitFormReason.REFUSED_RELIGION -> RefusalFormReason.REFUSED_RELIGION
        FaceExitFormReason.REFUSED_DATA_CONCERNS -> RefusalFormReason.REFUSED_DATA_CONCERNS
        FaceExitFormReason.REFUSED_PERMISSION -> RefusalFormReason.REFUSED_PERMISSION
        FaceExitFormReason.SCANNER_NOT_WORKING -> RefusalFormReason.SCANNER_NOT_WORKING
        FaceExitFormReason.REFUSED_NOT_PRESENT -> RefusalFormReason.REFUSED_NOT_PRESENT
        FaceExitFormReason.REFUSED_YOUNG -> RefusalFormReason.REFUSED_YOUNG
        FaceExitFormReason.OTHER -> RefusalFormReason.OTHER
    }
