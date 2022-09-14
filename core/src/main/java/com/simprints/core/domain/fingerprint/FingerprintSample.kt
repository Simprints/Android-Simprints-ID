package com.simprints.core.domain.fingerprint

import android.os.Parcelable
import com.simprints.moduleapi.fingerprint.IFingerIdentifier
import com.simprints.moduleapi.fingerprint.IFingerprintTemplateFormat
import kotlinx.parcelize.Parcelize
import java.util.*

@Parcelize
data class FingerprintSample(
    val fingerIdentifier: IFingerIdentifier,
    val template: ByteArray,
    val templateQualityScore: Int,
    val format: IFingerprintTemplateFormat,
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
fun List<FingerprintSample>.uniqueId(): String? {
    return if (this.isNotEmpty()) {
        UUID.nameUUIDFromBytes(
            concatTemplates()
        ).toString()
    } else {
        null
    }
}

private fun List<FingerprintSample>.concatTemplates(): ByteArray {
    return this.sortedBy { it.templateQualityScore }.fold(byteArrayOf()) { acc, sample ->
        acc + sample.template
    }
}
