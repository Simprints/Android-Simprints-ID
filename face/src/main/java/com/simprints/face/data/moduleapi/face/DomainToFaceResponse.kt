package com.simprints.face.data.moduleapi.face

import com.simprints.face.data.moduleapi.face.responses.FaceCaptureResponse
import com.simprints.face.data.moduleapi.face.responses.FaceMatchResponse
import com.simprints.face.data.moduleapi.face.responses.FaceResponse
import com.simprints.face.data.moduleapi.face.responses.FaceResponseType
import com.simprints.moduleapi.face.responses.*
import com.simprints.moduleapi.face.responses.entities.IFaceCaptureResult
import kotlinx.android.parcel.Parcelize

object DomainToFaceResponse {

    fun fromDomainToFaceResponse(faceResponse: FaceResponse): IFaceResponse =
        when (faceResponse.type) {
            FaceResponseType.CAPTURE -> fromDomainToFaceCaptureResponse(faceResponse as FaceCaptureResponse)
            FaceResponseType.MATCH -> fromDomainToFaceMatchResponse(faceResponse as FaceMatchResponse)
        }

    private fun fromDomainToFaceCaptureResponse(faceCaptureResponse: FaceCaptureResponse): IFaceCaptureResponseImpl =
        IFaceCaptureResponseImpl(faceCaptureResponse.capturingResult, IFaceResponseType.CAPTURE)

    private fun fromDomainToFaceMatchResponse(faceMatchResponse: FaceMatchResponse): IFaceMatchResponseImpl =
        IFaceMatchResponseImpl(faceMatchResponse.result, IFaceResponseType.MATCH)

}

@Parcelize
private class IFaceCaptureResponseImpl(
    override val capturingResult: List<IFaceCaptureResult>,
    override val type: IFaceResponseType
) : IFaceCaptureResponse

@Parcelize
private class IFaceMatchResponseImpl(
    override val result: List<IFaceMatchResult>,
    override val type: IFaceResponseType
): IFaceMatchResponse
