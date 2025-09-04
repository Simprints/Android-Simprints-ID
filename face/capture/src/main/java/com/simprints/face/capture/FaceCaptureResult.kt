package com.simprints.face.capture

import androidx.annotation.Keep
import com.simprints.core.domain.sample.CaptureSample
import com.simprints.core.domain.step.StepResult

@Keep
data class FaceCaptureResult(
    val referenceId: String,
    val results: List<CaptureSample>,
) : StepResult
