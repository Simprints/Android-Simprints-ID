package com.simprints.feature.enrollast

import androidx.annotation.Keep
import com.simprints.core.domain.sample.CaptureSample
import com.simprints.core.domain.sample.MatchConfidence
import com.simprints.core.domain.step.StepParams
import com.simprints.core.domain.tokenization.TokenizableString
import com.simprints.infra.config.store.models.FaceConfiguration
import com.simprints.infra.config.store.models.FingerprintConfiguration

@Keep
data class EnrolLastBiometricParams(
    val projectId: String,
    val userId: TokenizableString,
    val moduleId: TokenizableString,
    val steps: List<EnrolLastBiometricStepResult>,
) : StepParams

sealed class EnrolLastBiometricStepResult : StepParams {
    @Keep
    data class EnrolLastBiometricsResult(
        val subjectId: String?,
    ) : EnrolLastBiometricStepResult()

    @Keep
    data class FingerprintMatchResult(
        val results: List<MatchConfidence>,
        val sdk: FingerprintConfiguration.BioSdk,
    ) : EnrolLastBiometricStepResult()

    @Keep
    data class FaceMatchResult(
        val results: List<MatchConfidence>,
        val sdk: FaceConfiguration.BioSdk,
    ) : EnrolLastBiometricStepResult()

    @Keep
    data class CaptureResult(
        val referenceId: String,
        val results: List<CaptureSample>,
    ) : EnrolLastBiometricStepResult()
}
