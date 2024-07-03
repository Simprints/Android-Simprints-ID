package com.simprints.face.capture

import com.simprints.face.capture.screens.controller.FaceCaptureControllerFragmentArgs

object FaceCaptureContract {

    val DESTINATION = R.id.faceCaptureControllerFragment

    fun getArgs(
        samplesToCapture: Int,
    ) = FaceCaptureControllerFragmentArgs(
        FaceCaptureParams(
            samplesToCapture = samplesToCapture,
        )
    ).toBundle()
}
