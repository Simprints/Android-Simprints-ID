package com.simprints.eventsystem.event.domain.models

import androidx.annotation.Keep
import com.simprints.eventsystem.event.remote.models.ApiFingerComparisonStrategy

@Keep
enum class FingerComparisonStrategy {
    SAME_FINGER,
    CROSS_FINGER_USING_MEAN_OF_MAX;

    fun fromDomainToApi() = when (this) {
        SAME_FINGER -> ApiFingerComparisonStrategy.SAME_FINGER
        CROSS_FINGER_USING_MEAN_OF_MAX -> ApiFingerComparisonStrategy.CROSS_FINGER_USING_MEAN_OF_MAX
    }
}
