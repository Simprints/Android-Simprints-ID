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
    val matchConfidence: AppMatchConfidence,
    val verificationSuccess: Boolean? = null,
) : Parcelable {
    // Temporarily using match confidence as a proxy for tiers.
    val tier: AppResponseTier
        get() = when (matchConfidence) {
            AppMatchConfidence.NONE -> AppResponseTier.TIER_4
            AppMatchConfidence.LOW -> AppResponseTier.TIER_3
            AppMatchConfidence.MEDIUM -> AppResponseTier.TIER_2
            AppMatchConfidence.HIGH -> AppResponseTier.TIER_1
        }

    constructor(
        guid: String,
        confidenceScore: Float,
        decisionPolicy: DecisionPolicy,
        verificationMatchThreshold: Float? = null,
    ) : this(
        guid = guid,
        confidenceScore = confidenceScore.toInt(),
        matchConfidence = computeMatchConfidence(confidenceScore.toInt(), decisionPolicy),
        verificationSuccess = computeVerificationSuccess(confidenceScore.toInt(), verificationMatchThreshold),
    )

    companion object {
        fun computeMatchConfidence(
            confidenceScore: Int,
            policy: DecisionPolicy,
        ) = when {
            confidenceScore < policy.low -> AppMatchConfidence.NONE
            confidenceScore < policy.medium -> AppMatchConfidence.LOW
            confidenceScore < policy.high -> AppMatchConfidence.MEDIUM
            else -> AppMatchConfidence.HIGH
        }

        fun computeVerificationSuccess(
            confidenceScore: Int,
            verificationMatchThreshold: Float?,
        ) = when {
            verificationMatchThreshold == null -> null
            else -> confidenceScore >= verificationMatchThreshold
        }
    }
}
