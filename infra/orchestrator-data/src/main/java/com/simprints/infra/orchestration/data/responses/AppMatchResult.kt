package com.simprints.infra.orchestration.data.responses

import android.os.Parcelable
import com.simprints.core.ExcludedFromGeneratedTestCoverageReports
import com.simprints.core.domain.response.AppMatchConfidence
import com.simprints.core.domain.response.AppResponseTier
import com.simprints.infra.config.store.models.DecisionPolicy
import kotlinx.parcelize.Parcelize

@Parcelize
data class AppMatchResult(
    val guid: String,
    val confidenceScore: Int,
    val tier: AppResponseTier,
    val matchConfidence: AppMatchConfidence,
) : Parcelable {

    constructor(
        guid: String,
        confidenceScore: Float,
        decisionPolicy: DecisionPolicy,
    ) : this(
        guid,
        confidenceScore.toInt(),
        computeTier(confidenceScore),
        computeMatchConfidence(confidenceScore.toInt(), decisionPolicy)
    )

    companion object {

        fun computeTier(score: Float) = when {
            score < 20f -> AppResponseTier.TIER_5
            score < 35f -> AppResponseTier.TIER_4
            score < 50f -> AppResponseTier.TIER_3
            score < 75f -> AppResponseTier.TIER_2
            else -> AppResponseTier.TIER_1
        }

        fun computeMatchConfidence(confidenceScore: Int, policy: DecisionPolicy) = when {
            confidenceScore < policy.low -> AppMatchConfidence.NONE
            confidenceScore < policy.medium -> AppMatchConfidence.LOW
            confidenceScore < policy.high -> AppMatchConfidence.MEDIUM
            else -> AppMatchConfidence.HIGH
        }
    }
}
