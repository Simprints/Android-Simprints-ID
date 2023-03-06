package com.simprints.infra.events.remote.models

import androidx.annotation.Keep

@Keep
enum class ApiFingerComparisonStrategy {
    SAME_FINGER,
    CROSS_FINGER_USING_MEAN_OF_MAX
}
