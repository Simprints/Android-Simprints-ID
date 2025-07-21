package com.simprints.matcher

import androidx.annotation.Keep
import com.simprints.core.domain.step.StepResult
import com.simprints.infra.config.store.models.FaceConfiguration
import com.simprints.infra.config.store.models.FingerprintConfiguration

@Keep
interface MatchResult : StepResult {
    val results: List<MatchResultItem>
}

/**
 * This is required to bridge different interfaces from moduleApi module.
 */
@Keep
interface MatchResultItem : StepResult {
    val subjectId: String
    val confidence: Float
}

@Keep
data class FaceMatchResult(
    override val results: List<MatchResultItem>,
    val sdk: FaceConfiguration.BioSdk,
) : MatchResult {
    @Keep
    data class Item(
        override val subjectId: String,
        override val confidence: Float,
    ) : MatchResultItem
}

@Keep
data class FingerprintMatchResult(
    override val results: List<MatchResultItem>,
    val sdk: FingerprintConfiguration.BioSdk,
) : MatchResult {
    @Keep
    data class Item(
        override val subjectId: String,
        override val confidence: Float,
    ) : MatchResultItem
}
