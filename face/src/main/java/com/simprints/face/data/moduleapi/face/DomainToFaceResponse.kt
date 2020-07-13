package com.simprints.face.data.moduleapi.face

import com.simprints.face.data.moduleapi.face.responses.*
import com.simprints.moduleapi.face.responses.*
import com.simprints.moduleapi.face.responses.entities.IFaceCaptureResult
import kotlinx.android.parcel.IgnoredOnParcel
import kotlinx.android.parcel.Parcelize

object DomainToFaceResponse {

    fun fromDomainToFaceResponse(faceResponse: FaceResponse): IFaceResponse =
        when (faceResponse.type) {
            FaceResponseType.CAPTURE -> fromDomainToFaceCaptureResponse(faceResponse as FaceCaptureResponse)
            FaceResponseType.MATCH -> fromDomainToFaceMatchResponse(faceResponse as FaceMatchResponse)
            FaceResponseType.EXIT_FORM -> fromDomainToExitFormResponse(faceResponse as FaceExitFormResponse)
            FaceResponseType.ERROR -> fromDomainToErrorResponse(faceResponse as FaceErrorResponse)
            FaceResponseType.CONFIGURATION -> fromDomainToConfigurationResponse(faceResponse as FaceConfigurationResponse)
        }

    private fun fromDomainToFaceCaptureResponse(faceCaptureResponse: FaceCaptureResponse): IFaceCaptureResponseImpl =
        IFaceCaptureResponseImpl(faceCaptureResponse.capturingResult)

    private fun fromDomainToFaceMatchResponse(faceMatchResponse: FaceMatchResponse): IFaceMatchResponseImpl =
        IFaceMatchResponseImpl(faceMatchResponse.result)

    private fun fromDomainToExitFormResponse(faceExitFormResponse: FaceExitFormResponse): IFaceExitFormResponseImpl =
        IFaceExitFormResponseImpl(
            faceExitFormResponse.reason.fromDomainToExitReason(),
            faceExitFormResponse.extra
        )

    private fun fromDomainToErrorResponse(faceErrorResponse: FaceErrorResponse): IFaceResponse =
        IFaceErrorResponseImpl(faceErrorResponse.reason.fromDomainToFaceErrorReason())

    private fun fromDomainToConfigurationResponse(faceConfigurationResponse: FaceConfigurationResponse): IFaceResponse =
        IFaceConfigurationResponseImpl()
}

@Parcelize
private class IFaceCaptureResponseImpl(
    override val capturingResult: List<IFaceCaptureResult>
) : IFaceCaptureResponse {
    @IgnoredOnParcel
    override val type: IFaceResponseType = IFaceResponseType.CAPTURE
}

@Parcelize
private class IFaceMatchResponseImpl(
    override val result: List<IFaceMatchResult>
) : IFaceMatchResponse {
    @IgnoredOnParcel
    override val type: IFaceResponseType = IFaceResponseType.MATCH
}

@Parcelize
private class IFaceExitFormResponseImpl(
    override val reason: IFaceExitReason,
    override val extra: String
) : IFaceExitFormResponse {
    @IgnoredOnParcel
    override val type: IFaceResponseType = IFaceResponseType.EXIT_FORM
}

@Parcelize
private class IFaceErrorResponseImpl(override val reason: IFaceErrorReason) : IFaceErrorResponse {
    @IgnoredOnParcel
    override val type: IFaceResponseType = IFaceResponseType.ERROR
}

@Parcelize
private class IFaceConfigurationResponseImpl : IFaceConfigurationResponse {
    @IgnoredOnParcel
    override val type: IFaceResponseType = IFaceResponseType.CONFIGURATION
}
