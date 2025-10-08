package com.simprints.feature.enrollast

import androidx.annotation.Keep
import com.simprints.core.domain.step.StepParams
import com.simprints.core.domain.tokenization.TokenizableString
import com.simprints.feature.externalcredential.screens.search.model.ScannedCredential
import com.simprints.infra.config.store.models.FaceConfiguration
import com.simprints.infra.config.store.models.Finger
import com.simprints.infra.config.store.models.FingerprintConfiguration

@Keep
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
    data class FingerprintMatchResult(
        val results: List<MatchResult>,
        val sdk: FingerprintConfiguration.BioSdk,
    ) : EnrolLastBiometricStepResult()

    @Keep
    data class FaceMatchResult(
        val results: List<MatchResult>,
        val sdk: FaceConfiguration.BioSdk,
    ) : EnrolLastBiometricStepResult()

    @Keep
    data class FingerprintCaptureResult(
        val referenceId: String,
        val results: List<FingerTemplateCaptureResult>,
    ) : EnrolLastBiometricStepResult()

    @Keep
    data class FaceCaptureResult(
        val referenceId: String,
        val results: List<FaceTemplateCaptureResult>,
    ) : EnrolLastBiometricStepResult()
}

@Keep
data class MatchResult(
    val subjectId: String,
    val confidenceScore: Float,
) : StepParams

@Keep
data class FingerTemplateCaptureResult(
    val finger: Finger,
    val template: ByteArray,
    val templateQualityScore: Int,
    val format: String,
) : StepParams {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as FingerTemplateCaptureResult

        if (finger != other.finger) return false
        if (!template.contentEquals(other.template)) return false
        if (templateQualityScore != other.templateQualityScore) return false
        if (format != other.format) return false

        return true
    }

    override fun hashCode(): Int {
        var result = finger.hashCode()
        result = 31 * result + template.contentHashCode()
        result = 31 * result + templateQualityScore
        result = 31 * result + format.hashCode()
        return result
    }
}

@Keep
data class FaceTemplateCaptureResult(
    val template: ByteArray,
    val format: String,
) : StepParams {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as FaceTemplateCaptureResult

        if (!template.contentEquals(other.template)) return false
        if (format != other.format) return false

        return true
    }

    override fun hashCode(): Int {
        var result = template.contentHashCode()
        result = 31 * result + format.hashCode()
        return result
    }
}
