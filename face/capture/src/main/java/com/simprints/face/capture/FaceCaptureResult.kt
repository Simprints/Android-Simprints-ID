package com.simprints.face.capture

import androidx.annotation.Keep
import com.simprints.core.ExcludedFromGeneratedTestCoverageReports
import com.simprints.core.domain.step.StepResult
import com.simprints.infra.images.model.SecuredImageRef

@Keep
data class FaceCaptureResult(
    val referenceId: String,
    val results: List<Item>,
) : StepResult {
    @Keep
    data class Item(
        val captureEventId: String?,
        val index: Int,
        val sample: Sample?,
    ) : StepResult

    @Keep
    data class Sample(
        val faceId: String,
        val template: ByteArray,
        val imageRef: SecuredImageRef?,
        val format: String,
    ) : StepResult {
        @ExcludedFromGeneratedTestCoverageReports("Generated code")
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as Sample

            if (faceId != other.faceId) return false
            if (!template.contentEquals(other.template)) return false
            if (imageRef != other.imageRef) return false
            if (format != other.format) return false

            return true
        }

        @ExcludedFromGeneratedTestCoverageReports("Generated code")
        override fun hashCode(): Int {
            var result = faceId.hashCode()
            result = 31 * result + template.contentHashCode()
            result = 31 * result + (imageRef?.hashCode() ?: 0)
            result = 31 * result + format.hashCode()
            return result
        }
    }
}
