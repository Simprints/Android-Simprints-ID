package com.simprints.core.domain.fingerprint

import android.os.Parcelable
import com.simprints.core.ExcludedFromGeneratedTestCoverageReports
import com.simprints.core.domain.sample.SampleIdentifier
import kotlinx.parcelize.Parcelize
import java.util.UUID

@Parcelize
@ExcludedFromGeneratedTestCoverageReports("Data class with generated code")
data class FingerprintSample(
    val fingerIdentifier: SampleIdentifier,
    val template: ByteArray,
    val format: String,
    val referenceId: String,
    val id: String = UUID.randomUUID().toString(),
) : Parcelable {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as FingerprintSample

        if (fingerIdentifier != other.fingerIdentifier) return false
        if (!template.contentEquals(other.template)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = fingerIdentifier.hashCode()
        result = 31 * result + template.contentHashCode()
        return result
    }
}
