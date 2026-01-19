package com.simprints.face.capture

import androidx.annotation.Keep
import com.simprints.core.domain.step.StepParams
import com.simprints.infra.config.store.models.ModalitySdkType

@Keep
data class FaceCaptureParams(
    val samplesToCapture: Int,
    val faceSDK: ModalitySdkType,
) : StepParams
