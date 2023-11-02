package com.simprints.feature.orchestrator.model.responses

import com.simprints.infra.config.store.models.DecisionPolicy
import com.simprints.moduleapi.app.responses.IAppMatchConfidence
import com.simprints.moduleapi.app.responses.IAppMatchResult
import com.simprints.moduleapi.app.responses.IAppResponseTier
import kotlinx.parcelize.Parcelize

@Parcelize
data class AppMatchResult(
    override val guid: String,
    override val confidenceScore: Int,
    override val tier: IAppResponseTier,
    override val matchConfidence: IAppMatchConfidence
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
            score < 20f -> IAppResponseTier.TIER_5
            score < 35f -> IAppResponseTier.TIER_4
            score < 50f -> IAppResponseTier.TIER_3
            score < 75f -> IAppResponseTier.TIER_2
            else -> IAppResponseTier.TIER_1
        }

        fun computeMatchConfidence(confidenceScore: Int, policy: DecisionPolicy) = when {
            confidenceScore < policy.low -> IAppMatchConfidence.NONE
            confidenceScore < policy.medium -> IAppMatchConfidence.LOW
            confidenceScore < policy.high -> IAppMatchConfidence.MEDIUM
            else -> IAppMatchConfidence.HIGH
        }
    }
}
