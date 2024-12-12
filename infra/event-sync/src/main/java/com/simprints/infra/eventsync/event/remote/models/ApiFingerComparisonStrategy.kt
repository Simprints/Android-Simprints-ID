package com.simprints.infra.eventsync.event.remote.models

import androidx.annotation.Keep
import com.simprints.infra.events.event.domain.models.FingerComparisonStrategy

@Keep
internal enum class ApiFingerComparisonStrategy {
    SAME_FINGER,
    CROSS_FINGER_USING_MEAN_OF_MAX,
}

internal fun FingerComparisonStrategy.fromDomainToApi() = when (this) {
    FingerComparisonStrategy.SAME_FINGER -> ApiFingerComparisonStrategy.SAME_FINGER
    FingerComparisonStrategy.CROSS_FINGER_USING_MEAN_OF_MAX -> ApiFingerComparisonStrategy.CROSS_FINGER_USING_MEAN_OF_MAX
}
