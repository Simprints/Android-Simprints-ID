package com.simprints.face.data.moduleapi.face

import com.simprints.face.data.moduleapi.face.requests.*
import com.simprints.face.exceptions.InvalidFaceRequestException
import com.simprints.moduleapi.face.requests.IFaceCaptureRequest
import com.simprints.moduleapi.face.requests.IFaceConfigurationRequest
import com.simprints.moduleapi.face.requests.IFaceMatchRequest
import com.simprints.moduleapi.face.requests.IFaceRequest

object FaceToDomainRequest {
    fun fromFaceToDomainRequest(faceRequest: IFaceRequest): FaceRequest =
        when (faceRequest) {
            is IFaceCaptureRequest -> fromFaceToFaceCaptureRequest(faceRequest)
            is IFaceMatchRequest -> fromDomainToFaceMatchRequest(faceRequest)
            is IFaceConfigurationRequest -> fromFaceToFaceConfigurationRequest(faceRequest)
            else -> throw InvalidFaceRequestException("Exception if not a Match, Capture or Configuration Request")
        }

    private fun fromFaceToFaceCaptureRequest(faceRequest: IFaceCaptureRequest) =
        FaceCaptureRequest(faceRequest.nFaceSamplesToCapture)

    private fun fromDomainToFaceMatchRequest(faceRequest: IFaceMatchRequest) =
        FaceMatchRequest(
            faceRequest.probeFaceSamples.map { it.fromModuleApiToDomainFaceSample() },
            faceRequest.queryForCandidates
        )

    private fun fromFaceToFaceConfigurationRequest(faceRequest: IFaceConfigurationRequest) =
        FaceConfigurationRequest(faceRequest.projectId, faceRequest.deviceId)
}
