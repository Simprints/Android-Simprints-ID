package com.simprints.infra.matching

import androidx.annotation.Keep
import com.simprints.core.domain.sample.MatchConfidence
import com.simprints.core.domain.step.StepResult
import com.simprints.infra.config.store.models.FaceConfiguration
import com.simprints.infra.config.store.models.FingerprintConfiguration

@Keep
interface MatchResult : StepResult {
    val results: List<MatchConfidence>
}

@Keep
data class FaceMatchResult(
    override val results: List<MatchConfidence>,
    val sdk: FaceConfiguration.BioSdk,
) : MatchResult

@Keep
data class FingerprintMatchResult(
    override val results: List<MatchConfidence>,
    val sdk: FingerprintConfiguration.BioSdk,
) : MatchResult
