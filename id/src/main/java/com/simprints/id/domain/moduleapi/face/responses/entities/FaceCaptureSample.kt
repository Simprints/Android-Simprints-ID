package com.simprints.id.domain.moduleapi.face.responses.entities

import android.os.Parcelable
import com.simprints.core.images.model.SecuredImageRef
import com.simprints.id.data.db.images.domain.fromDomainToModuleApi
import com.simprints.id.data.db.images.domain.fromModuleApiToDomain
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

@Parcelize
private class IFaceSampleImpl(
    override val faceId: String,
    override val template: ByteArray,
    override val imageRef: ISecuredImageRef?
): IFaceSample
