package com.simprints.id.domain.moduleapi.face.requests

import com.simprints.moduleapi.face.requests.IFaceEnrolRequest
import com.simprints.moduleapi.face.requests.IFaceIdentifyRequest
import com.simprints.moduleapi.face.requests.IFaceRequest
import com.simprints.moduleapi.face.requests.IFaceVerifyRequest
import kotlinx.android.parcel.Parcelize

object DomainToFaceRequest {

    fun fromDomainToFaceRequest(faceRequest: FaceRequest): IFaceRequest =
        when (faceRequest) {
            is FaceEnrolRequest -> fromDomainToFaceEnrolRequest(faceRequest)
            is FaceVerifyRequest -> fromDomainToFaceVerifyRequest(faceRequest)
            is FaceIdentifyRequest -> fromDomainToFaceIdentifyRequest(faceRequest)
            else -> throw IllegalStateException("Invalid face request")
        }

    private fun fromDomainToFaceEnrolRequest(enrolRequest: FaceEnrolRequest): IFaceEnrolRequest =
        with(enrolRequest) {
            FaceEnrolRequestImpl(projectId, userId, moduleId)
        }

    private fun fromDomainToFaceVerifyRequest(verifyRequest: FaceVerifyRequest): IFaceVerifyRequest =
        with(verifyRequest) {
            FaceVerifyRequestImpl(projectId, userId, moduleId)
        }

    private fun fromDomainToFaceIdentifyRequest(identifyRequest: FaceIdentifyRequest): IFaceIdentifyRequest =

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
