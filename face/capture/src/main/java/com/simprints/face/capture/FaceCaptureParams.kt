package com.simprints.face.capture

import androidx.annotation.Keep
import com.simprints.core.domain.step.StepParams
import com.simprints.infra.config.store.models.ModalitySdkType
import kotlinx.serialization.Serializable

@Keep
@Serializable
data class FaceCaptureParams(
    val samplesToCapture: Int,
    val faceSDK: ModalitySdkType,
) : StepParams
