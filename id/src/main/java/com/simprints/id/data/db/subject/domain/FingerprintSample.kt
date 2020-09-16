package com.simprints.id.data.db.subject.domain

import android.os.Parcelable
import com.simprints.core.tools.EncodingUtils
import com.simprints.id.data.db.event.domain.models.subject.BiometricReference
import com.simprints.id.data.db.event.domain.models.subject.FingerprintReference
import com.simprints.id.data.db.event.domain.models.subject.FingerprintTemplate
import com.simprints.id.data.db.event.domain.models.subject.fromEventToPerson
import kotlinx.android.parcel.IgnoredOnParcel
import kotlinx.android.parcel.Parcelize
import java.util.*

@Parcelize
open class FingerprintSample(
    open val fingerIdentifier: FingerIdentifier,
    open val template: ByteArray,
    open val templateQualityScore: Int) : Parcelable {

    @IgnoredOnParcel
    open val id: String by lazy {
        UUID.nameUUIDFromBytes(template).toString()
    }

    companion object {
        fun extractFingerprintSamplesFromBiometricReferences(biometricReferences: List<BiometricReference>?) =
            biometricReferences?.filterIsInstance<FingerprintReference>()
                ?.firstOrNull()?.templates?.map { buildFingerprintSample(it) } ?: emptyList()

        private fun buildFingerprintSample(template: FingerprintTemplate): FingerprintSample {
            return FingerprintSample(
                template.finger.fromEventToPerson(),
                EncodingUtils.base64ToBytes(template.template),
                template.quality
            )
        }
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
