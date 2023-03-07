package com.simprints.infra.events.event.domain.models

import androidx.annotation.Keep
import com.simprints.infra.events.remote.models.ApiFingerComparisonStrategy

@Keep
enum class FingerComparisonStrategy {
    SAME_FINGER,
    CROSS_FINGER_USING_MEAN_OF_MAX;

    fun fromDomainToApi() = when (this) {
        SAME_FINGER -> ApiFingerComparisonStrategy.SAME_FINGER
        CROSS_FINGER_USING_MEAN_OF_MAX -> ApiFingerComparisonStrategy.CROSS_FINGER_USING_MEAN_OF_MAX
    }
}
