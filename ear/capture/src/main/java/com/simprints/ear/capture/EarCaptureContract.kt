package com.simprints.ear.capture

import android.os.Bundle
import com.simprints.ear.capture.screen.controller.EarCaptureControllerFragmentArgs

object EarCaptureContract {
    val DESTINATION = R.id.earCaptureControllerFragment

    fun getArgs(samplesToCapture: Int): Bundle = EarCaptureControllerFragmentArgs(samplesToCapture).toBundle()
}
