package com.simprints.face.capture

import android.os.Bundle
import com.simprints.face.capture.screens.controller.FaceCaptureControllerFragmentArgs

object FaceCaptureContract {
    val DESTINATION = R.id.faceCaptureControllerFragment

    fun getArgs(samplesToCapture: Int): Bundle = FaceCaptureControllerFragmentArgs(samplesToCapture).toBundle()
}
