package com.simprints.moduleapi.face.responses

import com.simprints.moduleapi.face.responses.entities.IFaceCaptureResult

interface IFaceCaptureResponse : IFaceResponse {
    val capturingResult: List<IFaceCaptureResult>
}
