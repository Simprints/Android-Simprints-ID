package com.simprints.id.domain.moduleapi.face.requests

import com.simprints.moduleapi.face.requests.IFaceCaptureRequest
import kotlinx.android.parcel.Parcelize

//Do not change the order of the parameters. Parcelize is not able to marshall correctly if type is the 2nd param
@Parcelize
data class FaceCaptureRequest(val nFaceSamplesToCapture: Int) : FaceRequest

fun FaceCaptureRequest.fromDomainToModuleApi(): IFaceCaptureRequest =
    IFaceCaptureRequestImpl(nFaceSamplesToCapture)

@Parcelize
private data class IFaceCaptureRequestImpl(override val nFaceSamplesToCapture: Int) : IFaceCaptureRequest
