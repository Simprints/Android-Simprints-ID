package com.simprints.fingerprint.capture

import androidx.annotation.Keep
import com.simprints.core.domain.sample.CaptureSample
import com.simprints.core.domain.step.StepResult

@Keep
data class FingerprintCaptureResult(
    val referenceId: String,
    var results: List<CaptureSample>,
) : StepResult
