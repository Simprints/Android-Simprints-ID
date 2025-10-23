package com.simprints.fingerprint.capture

import androidx.annotation.Keep
import com.simprints.core.ExcludedFromGeneratedTestCoverageReports
import com.simprints.core.domain.fingerprint.IFingerIdentifier
import com.simprints.core.domain.step.StepResult
import com.simprints.infra.images.model.SecuredImageRef

@Keep
data class FingerprintCaptureResult(
    val referenceId: String,
    var results: List<Item>,
) : StepResult {
    @Keep
    data class Item(
        val captureEventId: String?,
        val identifier: IFingerIdentifier,
        val sample: Sample?,
    ) : StepResult

    @Keep
    data class Sample(
        val fingerIdentifier: IFingerIdentifier,
        val template: ByteArray,
        val templateQualityScore: Int,
        val imageRef: SecuredImageRef?,
        val format: String,
    ) : StepResult {
        @ExcludedFromGeneratedTestCoverageReports("Generated code")
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as Sample

            if (templateQualityScore != other.templateQualityScore) return false
            if (fingerIdentifier != other.fingerIdentifier) return false
            if (!template.contentEquals(other.template)) return false
            if (imageRef != other.imageRef) return false
            if (format != other.format) return false

            return true
        }

        @ExcludedFromGeneratedTestCoverageReports("Generated code")
        override fun hashCode(): Int {
            var result = templateQualityScore
            result = 31 * result + fingerIdentifier.hashCode()
            result = 31 * result + template.contentHashCode()
            result = 31 * result + (imageRef?.hashCode() ?: 0)
            result = 31 * result + format.hashCode()
            return result
        }
    }
}
