package com.simprints.id.domain.moduleapi.face.responses.entities

import android.os.Parcelable
import com.simprints.core.images.SecuredImageRef
import com.simprints.core.images.fromDomainToModuleApi
import com.simprints.moduleapi.common.ISecuredImageRef
import com.simprints.moduleapi.face.responses.entities.IFaceSample
import kotlinx.android.parcel.Parcelize

@Parcelize
class FaceCaptureSample (
    val faceId: String,
    val template: ByteArray,
    val imageRef: SecuredImageRef?
): Parcelable

fun FaceCaptureSample.fromDomainToModuleApi(): IFaceSample =
    IFaceSampleImpl(faceId, template, imageRef?.fromDomainToModuleApi())

fun IFaceSample.fromModuleApiToDomain() =
    FaceCaptureSample(faceId, template, imageRef?.fromModuleApiToDomain())

fun ISecuredImageRef.fromModuleApiToDomain() = SecuredImageRef(path)

@Parcelize
private class IFaceSampleImpl(
    override val faceId: String,
    override val template: ByteArray,
    override val imageRef: ISecuredImageRef?
): IFaceSample
