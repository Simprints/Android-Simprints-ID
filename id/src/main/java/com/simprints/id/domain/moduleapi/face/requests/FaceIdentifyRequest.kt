package com.simprints.id.domain.moduleapi.face.requests

import com.simprints.id.domain.moduleapi.face.DomainToModuleApiFaceRequest
import kotlinx.android.parcel.Parcelize

@Parcelize
data class FaceIdentifyRequest(val projectId: String,
                               val userId: String,
                               val moduleId: String) : FaceRequest

fun FaceIdentifyRequest.fromDomainToApi() = DomainToModuleApiFaceRequest.fromDomainToModuleApiFaceRequest(this)
