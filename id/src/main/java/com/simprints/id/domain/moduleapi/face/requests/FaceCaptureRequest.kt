package com.simprints.id.domain.moduleapi.face.requests

import com.simprints.moduleapi.face.requests.IFaceCaptureRequest
import kotlinx.parcelize.Parcelize

//Do not change the order of the parameters. Parcelize is not able to marshall correctly if type is the 2nd param
@Parcelize
data class FaceCaptureRequest(
    val nFaceSamplesToCapture: Int,
    override val type: FaceRequestType = FaceRequestType.CAPTURE,
) : FaceRequest

@Parcelize
private data class IFaceCaptureRequestImpl(override val nFaceSamplesToCapture: Int) : IFaceCaptureRequest
