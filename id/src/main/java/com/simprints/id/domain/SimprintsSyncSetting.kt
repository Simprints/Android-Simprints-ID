package com.simprints.id.domain

import androidx.annotation.Keep

@Keep
enum class SimprintsSyncSetting {
    NONE,
    ONLY_BIOMETRICS,
    ONLY_ANALYTICS,
    ALL
}

fun SimprintsSyncSetting.canSyncAllDataToSimprints() = this.name == SimprintsSyncSetting.ALL.name

fun SimprintsSyncSetting.canSyncBiometricDataToSimprints() =
    this.name == SimprintsSyncSetting.ONLY_BIOMETRICS.name

fun SimprintsSyncSetting.canSyncAnalyticsDataToSimprints() =
    this.name == SimprintsSyncSetting.ONLY_ANALYTICS.name

fun SimprintsSyncSetting.canSyncDataToSimprints() = this.name != SimprintsSyncSetting.NONE.name

