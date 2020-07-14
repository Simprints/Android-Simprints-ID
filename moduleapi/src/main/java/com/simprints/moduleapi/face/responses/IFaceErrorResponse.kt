package com.simprints.moduleapi.face.responses

interface IFaceErrorResponse : IFaceResponse {
    val reason: IFaceErrorReason
}

enum class IFaceErrorReason {
    UNEXPECTED_ERROR,
    LICENSE_MISSING,
    LICENSE_INVALID,
    FACE_CONFIGURATION_ERROR
}
