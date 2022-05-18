package com.simprints.id.domain

import androidx.annotation.Keep

@Keep
enum class CosyncSetting {
    COSYNC_NONE,
    COSYNC_ONLY_BIOMETRICS,
    COSYNC_ONLY_ANALYTICS,
    COSYNC_ALL
}
