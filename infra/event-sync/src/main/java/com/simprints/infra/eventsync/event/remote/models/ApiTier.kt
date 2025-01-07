package com.simprints.infra.eventsync.event.remote.models

import androidx.annotation.Keep
import com.simprints.core.ExcludedFromGeneratedTestCoverageReports
import com.simprints.core.domain.response.AppMatchConfidence

@Keep
internal enum class ApiTier {
    TIER_1,
    TIER_2,
    TIER_3,
    TIER_4,
    TIER_5,
    ;

    companion object {
        /**
         * Tiers have been removed, but for older version not to break required field rules
         * we add a tier based on the match confidence.
         */
        @ExcludedFromGeneratedTestCoverageReports("Workaround to enable back-compatibility in old event versions")
        fun fromConfidence(matchConfidence: AppMatchConfidence) = when (matchConfidence) {
            AppMatchConfidence.NONE -> TIER_4
            AppMatchConfidence.LOW -> TIER_3
            AppMatchConfidence.MEDIUM -> TIER_2
            AppMatchConfidence.HIGH -> TIER_1
        }
    }
}
