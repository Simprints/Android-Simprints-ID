package com.simprints.core.domain.fingerprint

import android.os.Parcelable
import com.simprints.core.ExcludedFromGeneratedTestCoverageReports
import kotlinx.parcelize.Parcelize
import java.util.UUID

@Parcelize
@ExcludedFromGeneratedTestCoverageReports("Data class with generated code")
data class FingerprintSample(
    val fingerIdentifier: IFingerIdentifier,
    val template: FloatArray,
    val templateQualityScore: Int,
    val format: String,
    val id: String = UUID.randomUUID().toString(),
) : Parcelable {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as FingerprintSample

        if (fingerIdentifier != other.fingerIdentifier) return false
        if (!template.contentEquals(other.template)) return false
        if (templateQualityScore != other.templateQualityScore) return false

        return true
    }

    override fun hashCode(): Int {
        var result = fingerIdentifier.hashCode()
        result = 31 * result + template.contentHashCode()
        result = 31 * result + templateQualityScore
        return result
    }
}

// Generates a unique id for a list of samples.
// It concats the templates (sorted by quality score) and creates a UUID from that.
fun List<FingerprintSample>.uniqueId(): String? = UUID.randomUUID().toString()
