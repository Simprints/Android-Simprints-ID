package com.simprints.face.capture

import com.simprints.infra.config.store.models.ModalitySdkType

object FaceCaptureContract {
    val DESTINATION = R.id.faceCaptureControllerFragment

    fun getParams(
        samplesToCapture: Int,
        faceSDK: ModalitySdkType,
    ) = FaceCaptureParams(samplesToCapture, faceSDK)
}
