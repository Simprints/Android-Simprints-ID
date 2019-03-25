package com.simprints.id.domain.moduleapi.fingerprint.responses.entities

import com.simprints.id.domain.moduleapi.app.responses.entities.RefusalFormReason

enum class FingerprintRefusalFormReason {
    SCANNER_NOT_WORKING,
    REFUSED,
    OTHER
}

fun FingerprintRefusalFormReason.toAppRefusalFormReason() =
    when(this) {
        FingerprintRefusalFormReason.SCANNER_NOT_WORKING -> RefusalFormReason.SCANNER_NOT_WORKING
        FingerprintRefusalFormReason.REFUSED -> RefusalFormReason.REFUSED
        FingerprintRefusalFormReason.OTHER -> RefusalFormReason.OTHER
    }
