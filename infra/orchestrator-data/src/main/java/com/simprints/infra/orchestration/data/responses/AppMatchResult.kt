package com.simprints.infra.orchestration.data.responses

import android.os.Parcelable
import androidx.annotation.Keep
import com.simprints.core.domain.response.AppMatchConfidence
import com.simprints.core.domain.response.AppResponseTier
import com.simprints.infra.config.store.models.DecisionPolicy
import kotlinx.parcelize.Parcelize

@Keep
@Parcelize
data class AppMatchResult(
    val guid: String,
    val confidenceScore: Int,
    val tier: AppResponseTier,
    val matchConfidence: AppMatchConfidence,
    val verificationSuccess: Boolean? = null,
) : Parcelable {

    constructor(
        guid: String,
        confidenceScore: Float,
        decisionPolicy: DecisionPolicy,
        verificationMatchThreshold: Float? = null
    ) : this(
        guid = guid,
        confidenceScore = confidenceScore.toInt(),
        tier = computeTier(confidenceScore),
        matchConfidence = computeMatchConfidence(confidenceScore.toInt(), decisionPolicy),
        verificationSuccess = computeVerificationSuccess(confidenceScore.toInt(), verificationMatchThreshold),
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

        fun computeVerificationSuccess(confidenceScore: Int, verificationMatchThreshold: Float?) = when {
            verificationMatchThreshold == null -> null
            else -> confidenceScore >= verificationMatchThreshold
        }
    }
}
