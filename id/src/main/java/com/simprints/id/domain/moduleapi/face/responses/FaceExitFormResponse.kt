package com.simprints.id.domain.moduleapi.face.responses

import com.simprints.id.domain.moduleapi.app.responses.entities.RefusalFormReason
import com.simprints.moduleapi.face.responses.IFaceExitFormResponse
import com.simprints.moduleapi.face.responses.IFaceExitReason
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize

@Parcelize
data class FaceExitFormResponse(val reason: FaceExitReason, val extra: String) : FaceResponse {
    @IgnoredOnParcel
    override val type: FaceResponseType = FaceResponseType.EXIT_FORM
}

enum class FaceExitReason {
    REFUSED_RELIGION,
    REFUSED_DATA_CONCERNS,
    REFUSED_PERMISSION,
    APP_NOT_WORKING,
    REFUSED_NOT_PRESENT,
    REFUSED_YOUNG,
    OTHER;

    fun toAppRefusalFormReason() =
        when (this) {
            REFUSED_RELIGION -> RefusalFormReason.REFUSED_RELIGION
            REFUSED_DATA_CONCERNS -> RefusalFormReason.REFUSED_DATA_CONCERNS
            REFUSED_PERMISSION -> RefusalFormReason.REFUSED_PERMISSION
            REFUSED_YOUNG -> RefusalFormReason.REFUSED_YOUNG
            REFUSED_NOT_PRESENT -> RefusalFormReason.REFUSED_NOT_PRESENT
            APP_NOT_WORKING -> RefusalFormReason.APP_NOT_WORKING
            OTHER -> RefusalFormReason.OTHER
        }

}

fun IFaceExitFormResponse.fromModuleApiToDomain(): FaceExitFormResponse {
    val reason = when (this.reason) {
        IFaceExitReason.REFUSED_RELIGION -> FaceExitReason.REFUSED_RELIGION
        IFaceExitReason.REFUSED_DATA_CONCERNS -> FaceExitReason.REFUSED_DATA_CONCERNS
        IFaceExitReason.REFUSED_PERMISSION -> FaceExitReason.REFUSED_PERMISSION
        IFaceExitReason.APP_NOT_WORKING -> FaceExitReason.APP_NOT_WORKING
        IFaceExitReason.REFUSED_NOT_PRESENT -> FaceExitReason.REFUSED_NOT_PRESENT
        IFaceExitReason.REFUSED_YOUNG -> FaceExitReason.REFUSED_YOUNG
        IFaceExitReason.OTHER -> FaceExitReason.OTHER
    }

    return FaceExitFormResponse(reason, extra)
}
