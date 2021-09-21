package com.simprints.face.data.moduleapi.face.responses

import com.simprints.face.data.moduleapi.face.responses.entities.FaceCaptureResult
import kotlinx.parcelize.Parcelize

@Parcelize
data class FaceCaptureResponse(val capturingResult: List<FaceCaptureResult>,
                               override val type: FaceResponseType = FaceResponseType.CAPTURE) : FaceResponse
