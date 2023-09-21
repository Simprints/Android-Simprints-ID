package com.simprints.id.domain.moduleapi.face.responses

import com.simprints.feature.exitform.config.ExitFormOption
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
    SCANNER_NOT_WORKING,
    OTHER;

    fun toAppRefusalFormReason() =
        when (this) {
            REFUSED_RELIGION -> RefusalFormReason.REFUSED_RELIGION
            REFUSED_DATA_CONCERNS -> RefusalFormReason.REFUSED_DATA_CONCERNS
            REFUSED_PERMISSION -> RefusalFormReason.REFUSED_PERMISSION
            REFUSED_YOUNG -> RefusalFormReason.REFUSED_YOUNG
            REFUSED_NOT_PRESENT -> RefusalFormReason.REFUSED_NOT_PRESENT
            APP_NOT_WORKING -> RefusalFormReason.APP_NOT_WORKING
            SCANNER_NOT_WORKING -> RefusalFormReason.SCANNER_NOT_WORKING
            OTHER -> RefusalFormReason.OTHER
        }

    companion object {
        fun fromExitFormOption(option: ExitFormOption) = when (option) {
            ExitFormOption.ReligiousConcerns -> REFUSED_RELIGION
            ExitFormOption.DataConcerns -> REFUSED_DATA_CONCERNS
            ExitFormOption.NoPermission -> REFUSED_PERMISSION
            ExitFormOption.ScannerNotWorking -> SCANNER_NOT_WORKING
            ExitFormOption.AppNotWorking -> APP_NOT_WORKING
            ExitFormOption.PersonNotPresent -> REFUSED_NOT_PRESENT
            ExitFormOption.TooYoung -> REFUSED_YOUNG
            ExitFormOption.Other -> OTHER
        }
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
        IFaceExitReason.SCANNER_NOT_WORKING -> FaceExitReason.SCANNER_NOT_WORKING
        IFaceExitReason.OTHER -> FaceExitReason.OTHER
    }

    return FaceExitFormResponse(reason, extra)
}
