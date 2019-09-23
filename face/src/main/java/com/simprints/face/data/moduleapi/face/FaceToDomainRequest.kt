package com.simprints.face.data.moduleapi.face

import com.simprints.face.data.moduleapi.face.requests.FaceCaptureRequest
import com.simprints.face.data.moduleapi.face.requests.FaceRequest
import com.simprints.face.exceptions.InvalidFaceRequestException
import com.simprints.moduleapi.face.requests.IFaceCaptureRequest
import com.simprints.moduleapi.face.requests.IFaceRequest

object FaceToDomainRequest {
    fun fromFaceToDomainRequest(faceRequest: IFaceRequest): FaceRequest =
        when (faceRequest) {
            is IFaceCaptureRequest -> FaceCaptureRequest(faceRequest.nFaceSamplesToCapture)
            else -> throw InvalidFaceRequestException()
        }
}
