package com.simprints.id.domain

import androidx.annotation.Keep

@Keep
enum class CosyncSetting {
    NONE,
    ONLY_BIOMETRICS,
    ONLY_ANALYTICS,
    ALL
}

fun List<CosyncSetting>.canCoSyncAllData() = this.contains(CosyncSetting.ALL)

fun List<CosyncSetting>.canCoSyncBiometricData() =
    this.contains(CosyncSetting.ONLY_BIOMETRICS)

fun List<CosyncSetting>.canCoSyncAnalyticsData() =
    this.contains(CosyncSetting.ONLY_ANALYTICS)

fun List<CosyncSetting>.cannotCoSyncAnyData() = this.contains(CosyncSetting.NONE)
