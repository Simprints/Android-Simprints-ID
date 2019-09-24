package com.simprints.face.data.moduleapi.face

import com.simprints.face.data.moduleapi.face.responses.FaceCaptureResponse
import com.simprints.face.data.moduleapi.face.responses.FaceResponse
import com.simprints.face.exceptions.InvalidFaceResponseException
import com.simprints.moduleapi.face.responses.IFaceCaptureResponse
import com.simprints.moduleapi.face.responses.IFaceResponse
import com.simprints.moduleapi.face.responses.IFaceResponseType
import com.simprints.moduleapi.face.responses.entities.IFaceCaptureResult
import kotlinx.android.parcel.Parcelize

object DomainToFaceResponse {

    fun fromDomainToFaceResponse(faceResponse: FaceResponse): IFaceResponse =
        when (faceResponse) {
            is FaceCaptureResponse ->
                fromDomainToFaceCaptureResponse(faceResponse)
            else -> throw InvalidFaceResponseException()
        }

    private fun fromDomainToFaceCaptureResponse(faceCaptureResponse: FaceCaptureResponse): IFaceCaptureResponseImpl =
        IFaceCaptureResponseImpl(faceCaptureResponse.capturingResult, faceCaptureResponse.type)
}

@Parcelize
private class IFaceCaptureResponseImpl(
    override val capturingResult: List<IFaceCaptureResult>,
    override val type: IFaceResponseType
) : IFaceCaptureResponse
