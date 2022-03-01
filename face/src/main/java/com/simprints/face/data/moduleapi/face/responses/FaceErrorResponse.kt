package com.simprints.face.data.moduleapi.face.responses

import androidx.annotation.Keep
import com.simprints.face.error.ErrorType
import com.simprints.moduleapi.face.responses.IFaceErrorReason
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize

@Parcelize
data class FaceErrorResponse(val reason: FaceErrorReason) : FaceResponse {
    @IgnoredOnParcel
    override val type: FaceResponseType = FaceResponseType.ERROR
}

@Keep
enum class FaceErrorReason {
    UNEXPECTED_ERROR,
    LICENSE_MISSING,
    LICENSE_INVALID,
    BACKEND_MAINTENANCE_ERROR,
    CONFIGURATION_ERROR;

    fun fromDomainToFaceErrorReason(): IFaceErrorReason =
        when (this) {
            UNEXPECTED_ERROR -> IFaceErrorReason.UNEXPECTED_ERROR
            LICENSE_MISSING -> IFaceErrorReason.LICENSE_MISSING
            LICENSE_INVALID -> IFaceErrorReason.LICENSE_INVALID
            CONFIGURATION_ERROR -> IFaceErrorReason.FACE_CONFIGURATION_ERROR
            BACKEND_MAINTENANCE_ERROR -> IFaceErrorReason.BACKEND_MAINTENANCE_ERROR
        }

    companion object {
        fun fromErrorType(errorType: ErrorType): FaceErrorReason =
            when (errorType) {
                ErrorType.LICENSE_MISSING -> LICENSE_MISSING
                ErrorType.LICENSE_INVALID -> LICENSE_INVALID
                ErrorType.CONFIGURATION_ERROR -> CONFIGURATION_ERROR
                ErrorType.UNEXPECTED_ERROR -> UNEXPECTED_ERROR
                ErrorType.BACKEND_MAINTENANCE_ERROR -> BACKEND_MAINTENANCE_ERROR
            }
    }
}
