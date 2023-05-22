package com.simprints.id.domain.moduleapi.face.responses.entities

import android.os.Parcelable
import com.simprints.id.domain.moduleapi.images.fromDomainToModuleApi
import com.simprints.id.domain.moduleapi.images.fromModuleApiToDomain
import com.simprints.infra.images.model.SecuredImageRef
import com.simprints.moduleapi.common.ISecuredImageRef
import com.simprints.moduleapi.face.responses.entities.IFaceSample
import kotlinx.parcelize.Parcelize

@Parcelize
class FaceCaptureSample(
    val faceId: String,
    val template: ByteArray,
    val imageRef: SecuredImageRef?,
    val format: String
) : Parcelable

fun FaceCaptureSample.fromDomainToModuleApi(): IFaceSample =
    IFaceSampleImpl(faceId, template, imageRef?.fromDomainToModuleApi(), format)

fun IFaceSample.fromModuleApiToDomain() =
    FaceCaptureSample(faceId, template, imageRef?.fromModuleApiToDomain(), format)

@Parcelize
private class IFaceSampleImpl(
    override val faceId: String,
    override val template: ByteArray,
    override val imageRef: ISecuredImageRef?,
    override val format: String
) : IFaceSample
