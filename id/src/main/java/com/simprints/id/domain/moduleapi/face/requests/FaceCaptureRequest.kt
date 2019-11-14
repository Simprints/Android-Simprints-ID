package com.simprints.id.domain.moduleapi.face.requests

import com.simprints.id.domain.moduleapi.face.requests.FaceRequestType.*
import com.simprints.moduleapi.face.requests.IFaceCaptureRequest
import com.simprints.moduleapi.face.requests.IFaceRequestType
import kotlinx.android.parcel.Parcelize
import com.simprints.moduleapi.face.requests.IFaceRequestType.CAPTURE as ModuleApiCaptureType

//Do not change the order of the parameters. Parcelize is not able to marshall correctly if type is the 2nd param
@Parcelize
data class FaceCaptureRequest(override val type: FaceRequestType = CAPTURE,
                              val nFaceSamplesToCapture: Int) : FaceRequest

fun FaceCaptureRequest.fromDomainToModuleApi(): IFaceCaptureRequest =
    IFaceCaptureRequestImpl(nFaceSamplesToCapture)

@Parcelize
private data class IFaceCaptureRequestImpl(override val nFaceSamplesToCapture: Int,
                                           override val type: IFaceRequestType = ModuleApiCaptureType) : IFaceCaptureRequest
