package com.simprints.id.domain.moduleapi.face.requests

import com.simprints.id.domain.moduleapi.face.requests.FaceRequestType.*
import com.simprints.moduleapi.face.requests.IFaceIdentifyRequest
import com.simprints.moduleapi.face.requests.IFaceRequestType
import kotlinx.android.parcel.Parcelize
import com.simprints.moduleapi.face.requests.IFaceRequestType.IDENTIFY as ModuleApiIdentifyRequestType

@Parcelize
data class FaceIdentifyRequest(val projectId: String,
                               val userId: String,
                               val moduleId: String,
                               override val type: FaceRequestType = IDENTIFY) : FaceRequest

fun FaceIdentifyRequest.fromDomainToModuleApi(): IFaceIdentifyRequest = FaceIdentifyRequestImpl()

@Parcelize
private data class FaceIdentifyRequestImpl(override val type: IFaceRequestType = ModuleApiIdentifyRequestType) : IFaceIdentifyRequest
