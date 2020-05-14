package com.simprints.id.data.db.subject.domain

import android.os.Parcelable
import com.simprints.core.tools.EncodingUtils
import com.simprints.id.data.db.subject.domain.subjectevents.BiometricReference
import com.simprints.id.data.db.subject.domain.subjectevents.FingerprintReference
import com.simprints.id.data.db.subject.domain.subjectevents.FingerprintTemplate
import com.simprints.id.data.db.subject.domain.subjectevents.fromEventToPerson
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
            biometricReferences?.filterIsInstance(FingerprintReference::class.java)
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
