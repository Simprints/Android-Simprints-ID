package com.simprints.face.capture

import android.os.Bundle
import com.simprints.face.capture.screens.controller.FaceCaptureControllerFragmentArgs
import com.simprints.infra.config.store.models.FaceConfiguration

object FaceCaptureContract {
    val DESTINATION = R.id.faceCaptureControllerFragment

    fun getArgs(
        samplesToCapture: Int,
        faceSDK: FaceConfiguration.BioSdk,
    ): Bundle = FaceCaptureControllerFragmentArgs(FaceCaptureParams(samplesToCapture, faceSDK)).toBundle()
}
