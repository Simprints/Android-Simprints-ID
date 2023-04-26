package com.simprints.id.data.exitform

import com.simprints.feature.exitform.config.ExitFormOption

enum class ExitFormReason {
    REFUSED_RELIGION,
    REFUSED_DATA_CONCERNS,
    REFUSED_PERMISSION,
    SCANNER_NOT_WORKING,
    APP_NOT_WORKING,
    REFUSED_NOT_PRESENT,
    REFUSED_YOUNG,
    OTHER;

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
