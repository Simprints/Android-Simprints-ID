package com.simprints.id.domain

import androidx.annotation.Keep

@Keep
enum class SimprintsSyncSetting {
    SIM_SYNC_NONE,
    SIM_SYNC_ONLY_BIOMETRICS,
    SIM_SYNC_ONLY_ANALYTICS,
    SIM_SYNC_ALL
}


