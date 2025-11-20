package com.simprints.core.domain.sample

import androidx.annotation.Keep
import com.simprints.core.domain.common.Modality
import com.simprints.core.domain.step.StepResult

@Keep
data class CaptureIdentity(
    val referenceId: String,
    val modality: Modality,
    var samples: List<CaptureSample>,
) : StepResult
