package com.simprints.core.domain.sample

import android.os.Parcelable
import com.simprints.core.ExcludedFromGeneratedTestCoverageReports
import com.simprints.core.domain.common.Modality
import com.simprints.core.domain.sample.SampleIdentifier
import com.simprints.core.domain.step.StepResult
import kotlinx.parcelize.Parcelize
import java.util.UUID

@Parcelize
@ExcludedFromGeneratedTestCoverageReports("Data class with generated code")
data class CaptureSample(
    val captureEventId: String,
    val modality: Modality,
    val format: String,
    val template: ByteArray,
    val identifier: SampleIdentifier = SampleIdentifier.NONE,
) : StepResult,
    Parcelable {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as CaptureSample

        if (identifier != other.identifier) return false
        if (!template.contentEquals(other.template)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = identifier.hashCode()
        result = 31 * result + template.contentHashCode()
        return result
    }
}
