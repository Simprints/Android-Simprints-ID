package com.simprints.core.domain.sample

import android.os.Parcelable
import com.simprints.core.ExcludedFromGeneratedTestCoverageReports
import com.simprints.core.domain.modality.Modality
import kotlinx.parcelize.Parcelize
import java.util.UUID

@Parcelize
@ExcludedFromGeneratedTestCoverageReports("Data class with generated code")
data class Sample(
    val id: String = UUID.randomUUID().toString(),
    val referenceId: String,
    val format: String,
    val template: ByteArray,
    val modality: Modality,
    val identifier: SampleIdentifier = SampleIdentifier.NONE,
) : Parcelable {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Sample

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
