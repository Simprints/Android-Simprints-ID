package com.simprints.feature.enrollast

import androidx.annotation.Keep
import com.simprints.core.ExcludedFromGeneratedTestCoverageReports
import com.simprints.core.domain.common.ModalitySdkType
import com.simprints.core.domain.reference.BiometricReferenceCapture
import com.simprints.core.domain.reference.BiometricTemplate
import com.simprints.core.domain.sample.MatchComparisonResult
import com.simprints.core.domain.step.StepParams
import com.simprints.core.domain.tokenization.TokenizableString
import com.simprints.feature.externalcredential.screens.search.model.ScannedCredential

@Keep
@ExcludedFromGeneratedTestCoverageReports("Data class")
data class EnrolLastBiometricParams(
    val projectId: String,
    val userId: TokenizableString,
    val moduleId: TokenizableString,
    val steps: List<EnrolLastBiometricStepResult>,
    val scannedCredential: ScannedCredential?,
) : StepParams

sealed class EnrolLastBiometricStepResult : StepParams {
    @Keep
    data class EnrolLastBiometricsResult(
        val subjectId: String?,
    ) : EnrolLastBiometricStepResult()

    @Keep
    data class CaptureResult(
        val result: BiometricReferenceCapture,
    ) : EnrolLastBiometricStepResult()

    @Keep
    data class MatchResult(
        val results: List<MatchComparisonResult>,
        val sdk: ModalitySdkType,
    ) : EnrolLastBiometricStepResult()
}
