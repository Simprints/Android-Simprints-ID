package com.simprints.face.capture

import com.simprints.infra.config.store.models.FaceConfiguration

object FaceCaptureContract {
    val DESTINATION = R.id.faceCaptureControllerFragment

    fun getParams(
        samplesToCapture: Int,
        faceSDK: FaceConfiguration.BioSdk,
    ) = FaceCaptureParams(samplesToCapture, faceSDK)
}
