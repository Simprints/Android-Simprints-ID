package com.simprints.id.domain

import androidx.annotation.Keep

@Keep
enum class CosyncSetting {
    NONE,
    ONLY_BIOMETRICS,
    ONLY_ANALYTICS,
    ALL
}

fun CosyncSetting.canCoSyncAllData() = this.name == CosyncSetting.ALL.name

fun CosyncSetting.canCoSyncBiometricData() = this.name == CosyncSetting.ONLY_BIOMETRICS.name

fun CosyncSetting.canCoSyncAnalyticsData() = this.name == CosyncSetting.ONLY_ANALYTICS.name

fun CosyncSetting.cannotCoSyncAnyData() = this.name == CosyncSetting.NONE.name
