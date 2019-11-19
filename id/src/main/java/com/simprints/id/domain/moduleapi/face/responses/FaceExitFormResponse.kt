package com.simprints.id.domain.moduleapi.face.responses

import com.simprints.moduleapi.face.responses.IFaceExitFormResponse
import com.simprints.moduleapi.face.responses.IFaceExitReason
import kotlinx.android.parcel.IgnoredOnParcel
import kotlinx.android.parcel.Parcelize

@Parcelize
data class FaceExitFormResponse(val reason: FaceExitReason, val extra: String) : FaceResponse {
    @IgnoredOnParcel
    override val type: FaceResponseType = FaceResponseType.EXIT_FORM
}

enum class FaceExitReason {
    REFUSED_RELIGION,
    REFUSED_DATA_CONCERNS,
    REFUSED_PERMISSION,
    REFUSED_NOT_PRESENT,
    REFUSED_YOUNG,
    OTHER
}

fun IFaceExitFormResponse.formModuleApiToDomain(): FaceExitFormResponse {
    val reason = when (this.reason) {
        IFaceExitReason.REFUSED_RELIGION -> FaceExitReason.REFUSED_RELIGION
        IFaceExitReason.REFUSED_DATA_CONCERNS -> FaceExitReason.REFUSED_DATA_CONCERNS
        IFaceExitReason.REFUSED_PERMISSION -> FaceExitReason.REFUSED_PERMISSION
        IFaceExitReason.REFUSED_NOT_PRESENT -> FaceExitReason.REFUSED_NOT_PRESENT
        IFaceExitReason.REFUSED_YOUNG -> FaceExitReason.REFUSED_YOUNG
        IFaceExitReason.OTHER -> FaceExitReason.OTHER
    }

    return FaceExitFormResponse(reason, extra)
}
