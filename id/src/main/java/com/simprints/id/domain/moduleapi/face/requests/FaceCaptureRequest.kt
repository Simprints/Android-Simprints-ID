package com.simprints.id.domain.moduleapi.face.requests

import com.simprints.moduleapi.face.requests.IFaceCaptureRequest
import kotlinx.android.parcel.Parcelize

@Parcelize
data class FaceCaptureRequest(val nFaceSamplesToCapture: Int) : FaceRequest

fun FaceCaptureRequest.fromDomainToModuleApi(): IFaceCaptureRequest =
    IFaceCaptureRequestImpl(nFaceSamplesToCapture)

@Parcelize
private data class IFaceCaptureRequestImpl(override val nFaceSamplesToCapture: Int) : IFaceCaptureRequest
