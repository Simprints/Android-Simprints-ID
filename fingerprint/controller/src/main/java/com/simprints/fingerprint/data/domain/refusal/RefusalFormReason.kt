package com.simprints.fingerprint.data.domain.refusal

import com.simprints.feature.exitform.config.ExitFormOption

/**
 * This enum class represents the different reasons for submitting a refusal form.
 */

enum class RefusalFormReason {
    REFUSED_RELIGION,
    REFUSED_DATA_CONCERNS,
    REFUSED_PERMISSION,
    SCANNER_NOT_WORKING,
    REFUSED_NOT_PRESENT,
    REFUSED_YOUNG,
    APP_NOT_WORKING,
    OTHER;

    companion object {
        fun fromExitFormOption(option: ExitFormOption) = when (option) {
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
