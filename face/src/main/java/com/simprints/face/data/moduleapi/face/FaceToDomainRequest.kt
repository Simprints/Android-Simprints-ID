package com.simprints.face.data.moduleapi.face

import com.simprints.id.domain.moduleapi.face.requests.FaceEnrolRequest
import com.simprints.id.domain.moduleapi.face.requests.FaceRequest
import com.simprints.moduleapi.face.requests.IFaceEnrolRequest
import com.simprints.moduleapi.face.requests.IFaceRequest
import java.security.InvalidParameterException

object FaceToDomainRequest {

    fun fromFaceToDomainRequest(faceEnrolRequest: IFaceRequest): FaceRequest =
        when (faceEnrolRequest) {
            is IFaceEnrolRequest ->
                with(faceEnrolRequest) {
                    FaceEnrolRequest(projectId, moduleId, userId)
                }
            else -> throw InvalidParameterException("Invalid Face AppRequest") //StopShip
        }
}
