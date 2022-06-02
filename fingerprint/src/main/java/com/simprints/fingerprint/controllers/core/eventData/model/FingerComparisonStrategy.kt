package com.simprints.fingerprint.controllers.core.eventData.model

import androidx.annotation.Keep
import com.simprints.eventsystem.event.domain.models.FingerComparisonStrategy as FingerComparisonStrategyDomain
@Keep
enum class FingerComparisonStrategy {
    SAME_FINGER,
    CROSS_FINGER_USING_MEAN_OF_MAX
}
fun FingerComparisonStrategy.fromDomainToCore()= when(this){
    FingerComparisonStrategy.SAME_FINGER -> FingerComparisonStrategyDomain.SAME_FINGER
    FingerComparisonStrategy.CROSS_FINGER_USING_MEAN_OF_MAX -> FingerComparisonStrategyDomain.CROSS_FINGER_USING_MEAN_OF_MAX
}
