package com.simprints.face.controllers.core.events.model

import androidx.annotation.Keep
import com.simprints.feature.exitform.config.ExitFormOption
import com.simprints.moduleapi.face.responses.IFaceExitReason

@Keep
enum class RefusalAnswer {
    REFUSED_RELIGION,
    REFUSED_DATA_CONCERNS,
    REFUSED_PERMISSION,
    APP_NOT_WORKING,
    SCANNER_NOT_WORKING,
    REFUSED_NOT_PRESENT,
    REFUSED_YOUNG,
    OTHER;


    fun fromDomainToExitReason(): IFaceExitReason =
        when (this) {
            REFUSED_RELIGION -> IFaceExitReason.REFUSED_RELIGION
            REFUSED_DATA_CONCERNS -> IFaceExitReason.REFUSED_DATA_CONCERNS
            REFUSED_PERMISSION -> IFaceExitReason.REFUSED_PERMISSION
            APP_NOT_WORKING -> IFaceExitReason.APP_NOT_WORKING
            REFUSED_NOT_PRESENT -> IFaceExitReason.REFUSED_NOT_PRESENT
            REFUSED_YOUNG -> IFaceExitReason.REFUSED_YOUNG
            SCANNER_NOT_WORKING -> IFaceExitReason.SCANNER_NOT_WORKING
            OTHER -> IFaceExitReason.OTHER
        }

    companion object {
        fun fromExitFormOption(option: ExitFormOption): RefusalAnswer = when (option) {
            ExitFormOption.ReligiousConcerns -> REFUSED_RELIGION
            ExitFormOption.DataConcerns -> REFUSED_DATA_CONCERNS
            ExitFormOption.NoPermission -> REFUSED_PERMISSION
            ExitFormOption.AppNotWorking -> APP_NOT_WORKING
            ExitFormOption.ScannerNotWorking -> SCANNER_NOT_WORKING
            ExitFormOption.PersonNotPresent -> REFUSED_NOT_PRESENT
            ExitFormOption.TooYoung -> REFUSED_YOUNG
            ExitFormOption.Other -> OTHER
        }
    }
}
