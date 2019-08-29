package com.simprints.id.domain.moduleapi.face.responses.entities

import android.os.Parcelable
import com.simprints.core.images.SecuredImageRef
import com.simprints.moduleapi.common.ISecuredImageRef
import com.simprints.moduleapi.face.responses.entities.IFaceSample
import kotlinx.android.parcel.Parcelize

@Parcelize
data class FaceSample (
    val faceId: String,
    val template: ByteArray,
    val imageRef: SecuredImageRef?): Parcelable {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as FaceSample

        if (faceId != other.faceId) return false
        if (!template.contentEquals(other.template)) return false
        if (imageRef != other.imageRef) return false

        return true
    }

    override fun hashCode(): Int {
        var result = faceId.hashCode()
        result = 31 * result + template.contentHashCode()
        result = 31 * result + imageRef.hashCode()
        return result
    }
}

fun IFaceSample.fromModuleApiToDomain() =
    FaceSample(faceId, template, imageRef?.fromModuleApiToDomain())

fun ISecuredImageRef.fromModuleApiToDomain() =
    SecuredImageRef(uri)
