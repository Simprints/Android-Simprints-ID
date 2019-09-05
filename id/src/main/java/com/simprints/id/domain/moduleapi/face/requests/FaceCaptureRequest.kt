package com.simprints.id.domain.moduleapi.face.requests

import com.simprints.id.domain.moduleapi.face.requests.FaceRequestType.*
import com.simprints.moduleapi.face.requests.IFaceCaptureRequest
import com.simprints.moduleapi.face.requests.IFaceRequestType
import kotlinx.android.parcel.Parcelize
import com.simprints.moduleapi.face.requests.IFaceRequestType.CAPTURE as ModuleApiCaptureType

@Parcelize
data class FaceCaptureRequest(val nFaceSamplesToCapture: Int,
                              override val type: FaceRequestType = CAPTURE) : FaceRequest()

fun FaceCaptureRequest.fromDomainToModuleApi(): IFaceCaptureRequest =
    IFaceCaptureRequestImpl(nFaceSamplesToCapture)

@Parcelize
private data class IFaceCaptureRequestImpl(override val nFaceSamplesToCapture: Int,
                                           override val type: IFaceRequestType = ModuleApiCaptureType) : IFaceCaptureRequest
