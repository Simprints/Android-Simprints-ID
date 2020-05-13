package com.simprints.id.data.db.person.domain

import android.os.Parcelable
import com.simprints.core.tools.EncodingUtils
import com.simprints.id.data.db.person.domain.personevents.BiometricReference
import com.simprints.id.data.db.person.domain.personevents.FaceReference
import com.simprints.id.data.db.person.domain.personevents.FaceTemplate
import kotlinx.android.parcel.IgnoredOnParcel
import kotlinx.android.parcel.Parcelize
import java.util.*

@Parcelize
open class FaceSample(
    open val template: ByteArray) : Parcelable {

    @IgnoredOnParcel
    open val id: String by lazy {
        UUID.nameUUIDFromBytes(template).toString()
    }

    companion object {
        fun extractFaceSamplesFromBiometricReferences(biometricReferences: List<BiometricReference>?) =
            biometricReferences?.filterIsInstance(FaceReference::class.java)
                ?.firstOrNull()?.templates?.map { buildFaceSample(it) } ?: emptyList()

        private fun buildFaceSample(template: FaceTemplate) =
            FaceSample(EncodingUtils.base64ToBytes(template.template))
    }
}
