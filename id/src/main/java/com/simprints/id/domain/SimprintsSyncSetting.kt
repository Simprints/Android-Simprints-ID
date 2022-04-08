package com.simprints.id.domain

import androidx.annotation.Keep

@Keep
enum class SimprintsSyncSetting {
    NONE,
    ONLY_BIOMETRICS,
    ONLY_ANALYTICS,
    ALL
}

fun SimprintsSyncSetting.canSyncAllData() = this.name == SimprintsSyncSetting.ALL.name

fun SimprintsSyncSetting.canSyncBiometricData() =
    this.name == SimprintsSyncSetting.ONLY_BIOMETRICS.name

fun SimprintsSyncSetting.canSyncAnalyticsData() =
    this.name == SimprintsSyncSetting.ONLY_ANALYTICS.name

fun SimprintsSyncSetting.cannotSyncAnyData() = this.name == SimprintsSyncSetting.NONE.name

