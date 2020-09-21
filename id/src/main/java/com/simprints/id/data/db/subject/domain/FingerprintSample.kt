package com.simprints.id.data.db.subject.domain

import android.os.Parcelable
import kotlinx.android.parcel.IgnoredOnParcel
import kotlinx.android.parcel.Parcelize
import java.util.*

@Parcelize
data class FingerprintSample(
    val fingerIdentifier: FingerIdentifier,
    val template: ByteArray,
    val templateQualityScore: Int) : Parcelable {

    @IgnoredOnParcel
    val id: String by lazy {
        UUID.nameUUIDFromBytes(template).toString()
    }

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
fun List<FingerprintSample>.uniqueId() =
    UUID.nameUUIDFromBytes(
        contactTemplates()
    ).toString()

private fun List<FingerprintSample>.contactTemplates(): ByteArray {
    return this.sortedBy { it.templateQualityScore }.fold(byteArrayOf(), { acc, sample ->
        acc + sample.template
    })
}
