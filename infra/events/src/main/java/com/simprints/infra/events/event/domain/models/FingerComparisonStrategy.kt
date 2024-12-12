package com.simprints.infra.events.event.domain.models

import androidx.annotation.Keep

@Keep
enum class FingerComparisonStrategy {
    SAME_FINGER,
    CROSS_FINGER_USING_MEAN_OF_MAX,
}
