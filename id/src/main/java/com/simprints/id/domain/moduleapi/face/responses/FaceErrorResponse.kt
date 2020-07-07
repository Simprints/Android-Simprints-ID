package com.simprints.id.domain.moduleapi.face.responses

import com.simprints.id.domain.moduleapi.app.responses.AppErrorResponse
import com.simprints.moduleapi.face.responses.IFaceErrorReason
import com.simprints.moduleapi.face.responses.IFaceErrorResponse
import kotlinx.android.parcel.IgnoredOnParcel
import kotlinx.android.parcel.Parcelize

@Parcelize
data class FaceErrorResponse(val faceErrorReason: FaceErrorReason) : FaceResponse {
    @IgnoredOnParcel
    override val type: FaceResponseType = FaceResponseType.ERROR
}

fun IFaceErrorResponse.fromModuleApiToDomain() = FaceErrorResponse(reason.fromModuleApiToDomain())

enum class FaceErrorReason {
    UNEXPECTED_ERROR,
    FACE_LICENSE_MISSING,
    FACE_LICENSE_INVALID;

    fun toAppErrorReason(): AppErrorResponse.Reason =
        when (this) {
            UNEXPECTED_ERROR -> AppErrorResponse.Reason.UNEXPECTED_ERROR
            FACE_LICENSE_MISSING -> AppErrorResponse.Reason.FACE_LICENSE_MISSING
            FACE_LICENSE_INVALID -> AppErrorResponse.Reason.FACE_LICENSE_INVALID
        }
}

fun IFaceErrorReason.fromModuleApiToDomain() = when (this) {
    IFaceErrorReason.UNEXPECTED_ERROR -> FaceErrorReason.UNEXPECTED_ERROR
    IFaceErrorReason.LICENSE_MISSING -> FaceErrorReason.FACE_LICENSE_MISSING
    IFaceErrorReason.LICENSE_INVALID -> FaceErrorReason.FACE_LICENSE_INVALID
}

