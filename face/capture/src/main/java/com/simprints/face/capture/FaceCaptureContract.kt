package com.simprints.face.capture

import com.simprints.face.capture.screens.controller.FaceCaptureControllerFragmentArgs
import com.simprints.infra.protection.auxiliary.TemplateAuxData

object FaceCaptureContract {

    val DESTINATION = R.id.faceCaptureControllerFragment

    fun getArgs(
        samplesToCapture: Int,
        auxData: TemplateAuxData?,
    ) = FaceCaptureControllerFragmentArgs(
        FaceCaptureParams(
            samplesToCapture = samplesToCapture,
            auxData = auxData,
        )
    ).toBundle()
}
