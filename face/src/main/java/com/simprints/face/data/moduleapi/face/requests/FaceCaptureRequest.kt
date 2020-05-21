package com.simprints.face.data.moduleapi.face.requests

import kotlinx.android.parcel.Parcelize

@Parcelize
data class FaceCaptureRequest(val nFaceSamplesToCapture: Int) : FaceRequest
