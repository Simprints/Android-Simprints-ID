package com.simprints.id.domain.moduleapi.face.responses.entities

import android.os.Parcelable
import com.simprints.id.data.db.event.domain.models.face.FaceTemplateFormat
import com.simprints.id.data.db.event.domain.models.face.fromModuleApiToDomain
import com.simprints.id.data.images.model.SecuredImageRef
import com.simprints.id.domain.moduleapi.images.fromDomainToModuleApi
import com.simprints.id.domain.moduleapi.images.fromModuleApiToDomain
import com.simprints.moduleapi.common.ISecuredImageRef
import com.simprints.moduleapi.face.responses.entities.IFaceSample
import com.simprints.moduleapi.face.responses.entities.IFaceTemplateFormat
import kotlinx.android.parcel.Parcelize

@Parcelize
class FaceCaptureSample(
    val faceId: String,
    val template: ByteArray,
    val imageRef: SecuredImageRef?,
    val format: FaceTemplateFormat
) : Parcelable

fun FaceCaptureSample.fromDomainToModuleApi(): IFaceSample =
    IFaceSampleImpl(faceId, template, imageRef?.fromDomainToModuleApi(), format.fromDomainToModuleApi())

fun IFaceSample.fromModuleApiToDomain() =
    FaceCaptureSample(faceId, template, imageRef?.fromModuleApiToDomain(), format.fromModuleApiToDomain())

@Parcelize
private class IFaceSampleImpl(
    override val faceId: String,
    override val template: ByteArray,
    override val imageRef: ISecuredImageRef?,
    override val format: IFaceTemplateFormat
) : IFaceSample
