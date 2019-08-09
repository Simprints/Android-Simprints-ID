package com.simprints.id.domain.moduleapi.face

import com.simprints.id.domain.moduleapi.face.requests.FaceEnrolRequest
import com.simprints.id.domain.moduleapi.face.requests.FaceIdentifyRequest
import com.simprints.id.domain.moduleapi.face.requests.FaceRequest
import com.simprints.id.domain.moduleapi.face.requests.FaceVerifyRequest
import com.simprints.moduleapi.face.requests.IFaceEnrolRequest
import com.simprints.moduleapi.face.requests.IFaceIdentifyRequest
import com.simprints.moduleapi.face.requests.IFaceRequest
import com.simprints.moduleapi.face.requests.IFaceVerifyRequest
import kotlinx.android.parcel.Parcelize

object ModuleApiToDomainFaceRequest {

    fun fromDomainToModuleApiFaceRequest(faceRequest: FaceRequest): IFaceRequest =
        when (faceRequest) {
            is FaceEnrolRequest -> fromDomainToModuleApiFaceEnrolRequest(faceRequest)
            is FaceVerifyRequest -> fromDomainToModuleApiFaceVerifyRequest(faceRequest)
            is FaceIdentifyRequest -> fromDomainToModuleApiFaceIdentifyRequest(faceRequest)
            else -> throw IllegalStateException("Invalid face request")
        }

    private fun fromDomainToModuleApiFaceEnrolRequest(enrolRequest: FaceEnrolRequest): IFaceEnrolRequest =
        with(enrolRequest) {
            FaceEnrolRequestImpl(projectId, userId, moduleId)
        }

    private fun fromDomainToModuleApiFaceVerifyRequest(verifyRequest: FaceVerifyRequest): IFaceVerifyRequest =
        with(verifyRequest) {
            FaceVerifyRequestImpl(projectId, userId, moduleId)
        }

    private fun fromDomainToModuleApiFaceIdentifyRequest(identifyRequest: FaceIdentifyRequest): IFaceIdentifyRequest =

        with(identifyRequest) {
            FaceIdentifyRequestImpl(projectId, userId, moduleId)
        }
}

@Parcelize
private data class FaceEnrolRequestImpl(override val projectId: String,
                                        override val userId: String,
                                        override val moduleId: String) : IFaceEnrolRequest

@Parcelize
private data class FaceIdentifyRequestImpl(override val projectId: String,
                                           override val userId: String,
                                           override val moduleId: String) : IFaceIdentifyRequest

@Parcelize
private data class FaceVerifyRequestImpl(override val projectId: String,
                                         override val userId: String,
                                         override val moduleId: String) : IFaceVerifyRequest
