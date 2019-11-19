package com.simprints.id.domain.moduleapi.face.responses

import com.simprints.moduleapi.face.responses.IFaceErrorReason
import com.simprints.moduleapi.face.responses.IFaceErrorResponse
import kotlinx.android.parcel.IgnoredOnParcel
import kotlinx.android.parcel.Parcelize

@Parcelize
data class FaceErrorResponse(val faceErrorReason: FaceErrorReason): FaceResponse {
    @IgnoredOnParcel override val type: FaceResponseType = FaceResponseType.ERROR
}

fun IFaceErrorResponse.fromModuleApiToDomain() = FaceErrorResponse(error.fromModuleApiToDomain())

enum class FaceErrorReason {
    UNEXPECTED_ERROR
}

fun IFaceErrorReason.fromModuleApiToDomain() = when (this) {
    IFaceErrorReason.UNEXPECTED_ERROR -> FaceErrorReason.UNEXPECTED_ERROR
}

