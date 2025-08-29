package com.simprints.fingerprint.capture

import androidx.annotation.Keep
import com.simprints.core.domain.image.SecuredImageRef
import com.simprints.core.domain.sample.CaptureSample
import com.simprints.core.domain.sample.SampleIdentifier
import com.simprints.core.domain.step.StepResult

@Keep
data class FingerprintCaptureResult(
    val referenceId: String,
    var results: List<Item>,
) : StepResult {
    @Keep
    data class Item(
        val captureEventId: String?,
        val identifier: SampleIdentifier,
        val sample: CaptureSample?,
    ) : StepResult
}
