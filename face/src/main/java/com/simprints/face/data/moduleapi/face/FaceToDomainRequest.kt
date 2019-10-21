package com.simprints.face.data.moduleapi.face

import com.simprints.face.data.moduleapi.face.requests.FaceCaptureRequest
import com.simprints.face.data.moduleapi.face.requests.FaceRequest
import com.simprints.moduleapi.face.requests.IFaceCaptureRequest
import com.simprints.moduleapi.face.requests.IFaceRequest

object FaceToDomainRequest {
    fun fromFaceToDomainRequest(faceRequest: IFaceRequest): FaceRequest =
        when (faceRequest.type) {
            IFaceRequestType.CAPTURE -> fromFaceToFaceCaptureRequest(faceRequest as IFaceCaptureRequest)
            IFaceRequestType.VERIFY -> TODO()
            IFaceRequestType.IDENTIFY -> TODO()
        }

    private fun fromFaceToFaceCaptureRequest(faceRequest: IFaceCaptureRequest) = FaceCaptureRequest(faceRequest.nFaceSamplesToCapture)
}
