package com.simprints.id.domain

import androidx.annotation.Keep

@Keep
enum class SimprintsSyncSetting {
    NONE,
    ONLY_BIOMETRICS,
    ONLY_ANALYTICS,
    ALL
}

fun List<SimprintsSyncSetting>.canSyncAllData() = this.contains(SimprintsSyncSetting.ALL)

fun List<SimprintsSyncSetting>.canSyncBiometricData() =
    this.contains(SimprintsSyncSetting.ONLY_BIOMETRICS)

fun List<SimprintsSyncSetting>.canSyncAnalyticsData() =
    this.contains(SimprintsSyncSetting.ONLY_ANALYTICS)

fun List<SimprintsSyncSetting>.cannotSyncAnyData() = this.contains(SimprintsSyncSetting.NONE)

