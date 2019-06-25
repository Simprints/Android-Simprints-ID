package com.simprints.moduleapi.fingerprint.responses

interface IFingerprintRefusalFormResponse : IFingerprintResponse {

    val reason: IFingerprintRefusalReason
    val extra: String
}

enum class IFingerprintRefusalReason {
    REFUSED_RELIGION,
    REFUSED_DATA_CONCERNS,
    REFUSED_PERMISSION,
    SCANNER_NOT_WORKING,
    REFUSED_YOUNG,
    REFUSED_SICK,
    REFUSED_PREGNANT,
    OTHER
}
