package com.simprints.face.capture

import androidx.annotation.Keep
import com.simprints.core.domain.step.StepParams
import com.simprints.infra.config.store.models.FaceConfiguration

@Keep
data class FaceCaptureParams(
    val samplesToCapture: Int,
    val faceSDK: FaceConfiguration.BioSdk,
) : StepParams
