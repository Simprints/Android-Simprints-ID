package com.simprints.moduleapi.face.responses

interface IFaceExitFormResponse : IFaceResponse {

    val reason: IFingerprintExitReason
    val extra: String
}

enum class IFingerprintExitReason {
    REFUSED_RELIGION,
    REFUSED_DATA_CONCERNS,
    REFUSED_PERMISSION,
    SCANNER_NOT_WORKING,
    REFUSED_NOT_PRESENT,
    REFUSED_YOUNG,
    OTHER
}
