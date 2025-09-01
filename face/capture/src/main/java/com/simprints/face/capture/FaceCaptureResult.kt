package com.simprints.face.capture

import androidx.annotation.Keep
import com.simprints.core.domain.step.ModalityCaptureStepResult
import com.simprints.core.domain.step.ModalityCaptureStepResultItem

@Keep
data class FaceCaptureResult(
    override val referenceId: String,
    override val results: List<ModalityCaptureStepResultItem>,
) : ModalityCaptureStepResult
