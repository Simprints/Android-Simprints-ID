package com.simprints.feature.enrollast

import android.os.Parcelable
import androidx.annotation.Keep
import com.simprints.core.domain.tokenization.TokenizableString
import com.simprints.infra.config.store.models.Finger
import com.simprints.infra.config.store.models.FingerprintConfiguration
import kotlinx.parcelize.Parcelize

@Keep
@Parcelize
data class EnrolLastBiometricParams(
    val projectId: String,
    val userId: TokenizableString,
    val moduleId: TokenizableString,
    val steps: List<EnrolLastBiometricStepResult>,
) : Parcelable

sealed class EnrolLastBiometricStepResult : Parcelable {
    @Keep
    @Parcelize
    data class EnrolLastBiometricsResult(
        val subjectId: String?,
    ) : EnrolLastBiometricStepResult()

    @Keep
    @Parcelize
    data class FingerprintMatchResult(
        val results: List<MatchResult>,
        val sdk: FingerprintConfiguration.BioSdk,
    ) : EnrolLastBiometricStepResult()

    @Keep
    @Parcelize
    data class FaceMatchResult(
        val results: List<MatchResult>,
    ) : EnrolLastBiometricStepResult()

    @Keep
    @Parcelize
    data class FingerprintCaptureResult(
        val referenceId: String,
        val results: List<FingerTemplateCaptureResult>,
    ) : EnrolLastBiometricStepResult()

    @Keep
    @Parcelize
    data class FaceCaptureResult(
        val referenceId: String,
        val results: List<FaceTemplateCaptureResult>,
    ) : EnrolLastBiometricStepResult()
}

@Keep
@Parcelize
data class MatchResult(
    val subjectId: String,
    val confidenceScore: Float,
) : Parcelable

@Keep
@Parcelize
data class FingerTemplateCaptureResult(
    val finger: Finger,
    val template: ByteArray,
    val templateQualityScore: Int,
    val format: String,
) : Parcelable {
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
@Parcelize
data class FaceTemplateCaptureResult(
    val template: ByteArray,
    val format: String,
) : Parcelable {
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
