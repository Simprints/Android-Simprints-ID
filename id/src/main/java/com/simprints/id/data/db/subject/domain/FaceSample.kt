package com.simprints.id.data.db.subject.domain

import android.os.Parcelable
import com.simprints.id.data.db.event.domain.models.face.FaceTemplateFormat
import kotlinx.android.parcel.IgnoredOnParcel
import kotlinx.android.parcel.Parcelize
import java.util.*

@Parcelize
data class FaceSample(val template: ByteArray, val format: FaceTemplateFormat) : Parcelable {

    @IgnoredOnParcel
    val id: String by lazy {
        UUID.nameUUIDFromBytes(template).toString()
    }

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

private fun List<FaceSample>.concatTemplates(): ByteArray {
    return this.sortedBy { it.template.hashCode() }.fold(byteArrayOf(), { acc, sample ->
        acc + sample.template
    })
}
