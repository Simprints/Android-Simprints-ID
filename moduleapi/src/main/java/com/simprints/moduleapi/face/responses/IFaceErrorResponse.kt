package com.simprints.moduleapi.face.responses

interface IFaceErrorResponse : IFaceResponse {
    val reason: IFaceErrorReason
}

// TODO: create new errors: LICENSE_MISSING, LICENSE_INVALID
enum class IFaceErrorReason {
    UNEXPECTED_ERROR,
    FACE_CONFIGURATION_ERROR
}
