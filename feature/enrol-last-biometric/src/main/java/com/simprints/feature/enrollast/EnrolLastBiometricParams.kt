package com.simprints.feature.enrollast

import androidx.annotation.Keep
import com.simprints.core.domain.modality.Modality
import com.simprints.core.domain.sample.CaptureSample
import com.simprints.core.domain.step.StepParams
import com.simprints.core.domain.tokenization.TokenizableString
import com.simprints.infra.config.store.models.ModalitySdkType

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
    data class MatchResult(
        val results: List<Item>,
        val modality: Modality,
        val sdk: ModalitySdkType,
    ) : EnrolLastBiometricStepResult() {
        @Keep
        data class Item(
            val subjectId: String,
            val confidenceScore: Float,
        ) : StepParams
    }

    @Keep
    data class CaptureResult(
        val referenceId: String,
        val modality: Modality,
        val results: List<CaptureSample>,
    ) : EnrolLastBiometricStepResult()
}
