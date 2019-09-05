package com.simprints.id.domain.moduleapi.face.requests

import com.simprints.id.domain.moduleapi.face.requests.FaceRequestType.*
import com.simprints.moduleapi.face.requests.IFaceRequestType
import com.simprints.moduleapi.face.requests.IFaceVerifyRequest
import kotlinx.android.parcel.Parcelize
import com.simprints.moduleapi.face.requests.IFaceRequestType.VERIFY as ModuleApiVerifyRequestType
@Parcelize
data class FaceVerifyRequest(val projectId: String,
                             val userId: String,
                             val moduleId: String,
                             override val type: FaceRequestType = VERIFY) : FaceRequest()

fun FaceVerifyRequest.fromDomainToModuleApi(): IFaceVerifyRequest = FaceVerifyRequestImpl()

@Parcelize
private data class FaceVerifyRequestImpl(override val type: IFaceRequestType = ModuleApiVerifyRequestType) : IFaceVerifyRequest
