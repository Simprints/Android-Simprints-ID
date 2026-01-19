package com.simprints.feature.enrollast

import androidx.annotation.Keep
import com.simprints.core.ExcludedFromGeneratedTestCoverageReports
import com.simprints.core.domain.capture.BiometricReferenceCapture
import com.simprints.core.domain.comparison.ComparisonResult
import com.simprints.core.domain.step.StepParams
import com.simprints.core.domain.tokenization.TokenizableString
import com.simprints.feature.externalcredential.screens.search.model.ScannedCredential
import com.simprints.infra.config.store.models.ModalitySdkType
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Keep
@ExcludedFromGeneratedTestCoverageReports("Data class")
@Serializable
data class EnrolLastBiometricParams(
    val projectId: String,
    val userId: TokenizableString,
    val moduleId: TokenizableString,
    val steps: List<EnrolLastBiometricStepResult>,
    val scannedCredential: ScannedCredential?,
) : StepParams

@Serializable
sealed class EnrolLastBiometricStepResult : StepParams {
    @Keep
    @Serializable
    @SerialName("EnrolLastBiometricStepResult.EnrolLastBiometricsResult")
    data class EnrolLastBiometricsResult(
        val subjectId: String?,
    ) : EnrolLastBiometricStepResult()

    @Keep
    @Serializable
    @SerialName("EnrolLastBiometricStepResult.CaptureResult")
    data class CaptureResult(
        val result: BiometricReferenceCapture,
    ) : EnrolLastBiometricStepResult()

    @Keep
    @Serializable
    @SerialName("EnrolLastBiometricStepResult.MatchResult")
    data class MatchResult(
        val results: List<ComparisonResult>,
        val sdk: ModalitySdkType,
    ) : EnrolLastBiometricStepResult()
}
