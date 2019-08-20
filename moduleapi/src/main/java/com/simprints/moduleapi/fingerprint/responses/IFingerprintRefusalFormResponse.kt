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
    REFUSED_NOT_PRESENT,
    REFUSED_YOUNG,
    OTHER
}
