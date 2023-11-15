package com.simprints.core.domain.face

import android.os.Parcelable
import com.simprints.core.ExcludedFromGeneratedTestCoverageReports
import kotlinx.parcelize.Parcelize
import java.util.UUID
import javax.annotation.processing.Generated

@Parcelize
@ExcludedFromGeneratedTestCoverageReports("Data class with generated code")
data class FaceSample(
    val template: ByteArray,
    val format: String,
    val id: String = UUID.randomUUID().toString(),
) : Parcelable {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as FaceSample

        if (!template.contentEquals(other.template)) return false

        return true
    }

    override fun hashCode(): Int {
        return template.contentHashCode()
    }
}

// Generates a unique id for a list of samples.
// It concats the templates (sorted by quality score) and creates a UUID from that. Or null if there
// are not templates
fun List<FaceSample>.uniqueId(): String? {
    return if (this.isNotEmpty()) {
        UUID.nameUUIDFromBytes(
            concatTemplates()
        ).toString()
    } else {
        null
    }
}

fun List<FaceSample>.concatTemplates(): ByteArray {
    return this.sortedBy { it.template.contentHashCode() }.fold(byteArrayOf()) { acc, sample ->
        acc + sample.template
    }
}
