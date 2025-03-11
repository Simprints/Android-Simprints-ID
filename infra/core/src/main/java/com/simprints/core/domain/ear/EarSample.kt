package com.simprints.core.domain.ear

import android.os.Parcelable
import com.simprints.core.ExcludedFromGeneratedTestCoverageReports
import com.simprints.core.domain.face.FaceSample
import kotlinx.parcelize.Parcelize
import java.util.UUID

@Parcelize
@ExcludedFromGeneratedTestCoverageReports("Data class with generated code")
data class EarSample(
    val template: ByteArray,
    val format: String,
    val referenceId: String,
    val id: String = UUID.randomUUID().toString(),
) : Parcelable {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as FaceSample

        if (!template.contentEquals(other.template)) return false

        return true
    }

    override fun hashCode(): Int = template.contentHashCode()
}
