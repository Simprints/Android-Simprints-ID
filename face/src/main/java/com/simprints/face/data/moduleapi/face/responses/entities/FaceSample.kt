package com.simprints.face.data.moduleapi.face.responses.entities

import com.simprints.moduleapi.common.ISecuredImageRef
import com.simprints.moduleapi.face.responses.entities.IFaceSample
import com.simprints.moduleapi.face.responses.entities.IFaceTemplateFormat
import kotlinx.android.parcel.Parcelize

@Parcelize
data class FaceSample(
    override val faceId: String,
    override val template: ByteArray,
    override val imageRef: ISecuredImageRef?,
    override val format: IFaceTemplateFormat
) : IFaceSample
