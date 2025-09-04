package com.simprints.feature.enrollast

import androidx.annotation.Keep
import com.simprints.core.domain.common.ModalitySdkType
import com.simprints.core.domain.sample.CaptureSample
import com.simprints.core.domain.sample.MatchConfidence
import com.simprints.core.domain.step.StepParams
import com.simprints.core.domain.tokenization.TokenizableString

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
    data class CaptureResult(
        val referenceId: String,
        val results: List<CaptureSample>,
    ) : EnrolLastBiometricStepResult()

    @Keep
    data class MatchResult(
        val results: List<MatchConfidence>,
        val sdk: ModalitySdkType,
    ) : EnrolLastBiometricStepResult()
}
