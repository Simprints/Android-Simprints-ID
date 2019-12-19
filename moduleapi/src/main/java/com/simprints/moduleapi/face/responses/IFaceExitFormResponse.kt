package com.simprints.moduleapi.face.responses

interface IFaceExitFormResponse : IFaceResponse {

    val reason: IFaceExitReason
    val extra: String
}

enum class IFaceExitReason {
    REFUSED_RELIGION,
    REFUSED_DATA_CONCERNS,
    REFUSED_PERMISSION,
    REFUSED_NOT_PRESENT,
    REFUSED_YOUNG,
    OTHER
}
