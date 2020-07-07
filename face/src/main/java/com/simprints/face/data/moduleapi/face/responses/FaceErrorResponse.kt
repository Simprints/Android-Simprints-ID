package com.simprints.face.data.moduleapi.face.responses

import androidx.annotation.Keep
import com.simprints.face.error.ErrorType
import com.simprints.moduleapi.face.responses.IFaceErrorReason
import kotlinx.android.parcel.IgnoredOnParcel
import kotlinx.android.parcel.Parcelize

@Parcelize
data class FaceErrorResponse(val reason: FaceErrorReason) : FaceResponse {
    @IgnoredOnParcel
    override val type: FaceResponseType = FaceResponseType.ERROR
}

@Keep
enum class FaceErrorReason {
    UNEXPECTED_ERROR,
    LICENSE_MISSING,
    LICENSE_INVALID;

    fun fromDomainToFaceErrorReason(): IFaceErrorReason =
        when (this) {
            UNEXPECTED_ERROR -> IFaceErrorReason.UNEXPECTED_ERROR
            LICENSE_MISSING -> IFaceErrorReason.LICENSE_MISSING
            LICENSE_INVALID -> IFaceErrorReason.LICENSE_INVALID
        }

    companion object {
        fun fromErrorType(errorType: ErrorType): FaceErrorReason =
            when (errorType) {
                ErrorType.LICENSE_MISSING -> LICENSE_MISSING
                ErrorType.LICENSE_INVALID -> LICENSE_INVALID
            }
    }
}
