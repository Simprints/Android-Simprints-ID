package com.simprints.face.data.moduleapi.face

import com.simprints.face.data.moduleapi.face.requests.FaceCaptureRequest
import com.simprints.face.data.moduleapi.face.requests.FaceMatchRequest
import com.simprints.face.data.moduleapi.face.requests.FaceRequest
import com.simprints.face.data.moduleapi.face.requests.fromModuleApiToDomainFaceSample
import com.simprints.moduleapi.face.requests.IFaceCaptureRequest
import com.simprints.moduleapi.face.requests.IFaceMatchRequest
import com.simprints.moduleapi.face.requests.IFaceRequest

object FaceToDomainRequest {
    fun fromFaceToDomainRequest(faceRequest: IFaceRequest): FaceRequest =
        when (faceRequest) {
            is IFaceCaptureRequest -> fromFaceToFaceCaptureRequest(faceRequest)
            is IFaceMatchRequest -> fromDomainToFaceMatchRequest(faceRequest)
            else -> TODO("Exception if not a Match or Capture Request")
        }

    private fun fromFaceToFaceCaptureRequest(faceRequest: IFaceCaptureRequest) = FaceCaptureRequest(faceRequest.nFaceSamplesToCapture)

    private fun fromDomainToFaceMatchRequest(faceRequest: IFaceMatchRequest) =
        FaceMatchRequest(
            faceRequest.probeFaceSamples.map { it.fromModuleApiToDomainFaceSample() },
            faceRequest.queryForCandidates
        )
}
