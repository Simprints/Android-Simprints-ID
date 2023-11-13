package com.simprints.feature.orchestrator.model.responses

import com.simprints.infra.config.store.models.DecisionPolicy
import com.simprints.core.domain.response.AppMatchConfidence
import com.simprints.infra.orchestration.moduleapi.app.responses.IAppMatchResult
import com.simprints.core.domain.response.AppResponseTier
import kotlinx.parcelize.Parcelize

@Parcelize
internal data class AppMatchResult(
    override val guid: String,
    override val confidenceScore: Int,
    override val tier: AppResponseTier,
    override val matchConfidence: AppMatchConfidence
) : IAppMatchResult {

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
