package com.simprints.face.data.moduleapi.face.requests

import kotlinx.parcelize.Parcelize

@Parcelize
data class FaceCaptureRequest(val nFaceSamplesToCapture: Int) : FaceRequest
