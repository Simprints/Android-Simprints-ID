package com.simprints.moduleapi.face.responses

interface IFaceErrorResponse {
    val error: IFaceErrorReason
}

enum class IFaceErrorReason {
    UNEXPECTED_ERROR
}
