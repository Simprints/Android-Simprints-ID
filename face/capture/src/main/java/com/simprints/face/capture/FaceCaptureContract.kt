package com.simprints.face.capture

import android.os.Bundle
import com.simprints.face.capture.screens.controller.FaceCaptureControllerFragmentArgs

object FaceCaptureContract {

    const val RESULT = "face_capture_result"

    fun getArgs(
        samplesToCapture: Int,
    ): Bundle = FaceCaptureControllerFragmentArgs(samplesToCapture).toBundle()
}
