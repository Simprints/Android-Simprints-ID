package com.simprints.face.data.moduleapi.face

import com.simprints.face.data.moduleapi.face.requests.FaceEnrolRequest
import com.simprints.face.data.moduleapi.face.requests.FaceIdentifyRequest
import com.simprints.face.data.moduleapi.face.requests.FaceRequest
import com.simprints.face.data.moduleapi.face.requests.FaceVerifyRequest
import com.simprints.face.exceptions.InvalidFaceRequestException
import com.simprints.moduleapi.face.requests.IFaceEnrolRequest
import com.simprints.moduleapi.face.requests.IFaceIdentifyRequest
import com.simprints.moduleapi.face.requests.IFaceRequest
import com.simprints.moduleapi.face.requests.IFaceVerifyRequest
import java.security.InvalidParameterException

object FaceToDomainRequest {

    fun fromFaceToDomainRequest(faceRequest: IFaceRequest): FaceRequest =
        when (faceRequest) {
            is IFaceEnrolRequest ->
                with(faceRequest) {
                    FaceEnrolRequest(projectId, moduleId, userId)
                }
            is IFaceIdentifyRequest ->
                with(faceRequest) {
                    FaceIdentifyRequest(projectId, moduleId, userId)
                }
            is IFaceVerifyRequest ->
                with(faceRequest) {
                    FaceVerifyRequest(projectId, moduleId, userId)
                }
            else -> throw InvalidFaceRequestException()
        }
}
