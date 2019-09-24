package com.simprints.id.domain.moduleapi.face.responses.entities

import android.os.Parcelable
import com.simprints.core.images.SecuredImageRef
import com.simprints.moduleapi.common.ISecuredImageRef
import com.simprints.moduleapi.face.responses.entities.IFaceSample
import kotlinx.android.parcel.Parcelize

@Parcelize
class FaceCaptureSample (
    val faceId: String,
    val template: ByteArray,
    val imageRef: SecuredImageRef?): Parcelable {
}

fun IFaceSample.fromModuleApiToDomain() =
    FaceCaptureSample(faceId, template, imageRef?.fromModuleApiToDomain())

fun ISecuredImageRef.fromModuleApiToDomain() =
    SecuredImageRef(uri)
