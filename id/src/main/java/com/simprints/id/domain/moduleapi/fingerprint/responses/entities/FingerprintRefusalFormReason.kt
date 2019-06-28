package com.simprints.id.domain.moduleapi.fingerprint.responses.entities

import com.simprints.id.domain.moduleapi.app.responses.entities.RefusalFormReason

enum class FingerprintRefusalFormReason {
    REFUSED_RELIGION,
    REFUSED_DATA_CONCERNS,
    REFUSED_PERMISSION,
    SCANNER_NOT_WORKING,
    REFUSED_NOT_PRESENT,
    REFUSED_YOUNG,
    REFUSED_SICK,
    REFUSED_PREGNANT,
    OTHER
}

fun FingerprintRefusalFormReason.toAppRefusalFormReason() =
    when(this) {
        FingerprintRefusalFormReason.REFUSED_RELIGION -> RefusalFormReason.REFUSED_RELIGION
        FingerprintRefusalFormReason.REFUSED_DATA_CONCERNS -> RefusalFormReason.REFUSED_DATA_CONCERNS
        FingerprintRefusalFormReason.REFUSED_PERMISSION -> RefusalFormReason.REFUSED_PERMISSION
        FingerprintRefusalFormReason.REFUSED_YOUNG -> RefusalFormReason.REFUSED_YOUNG
        FingerprintRefusalFormReason.REFUSED_NOT_PRESENT -> RefusalFormReason.REFUSED_NOT_PRESENT
        FingerprintRefusalFormReason.REFUSED_SICK -> RefusalFormReason.REFUSED_SICK
        FingerprintRefusalFormReason.REFUSED_PREGNANT -> RefusalFormReason.REFUSED_PREGNANT
        FingerprintRefusalFormReason.SCANNER_NOT_WORKING -> RefusalFormReason.SCANNER_NOT_WORKING
        FingerprintRefusalFormReason.OTHER -> RefusalFormReason.OTHER
    }
