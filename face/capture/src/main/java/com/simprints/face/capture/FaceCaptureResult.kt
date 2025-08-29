package com.simprints.face.capture

import androidx.annotation.Keep
import com.simprints.core.domain.image.SecuredImageRef
import com.simprints.core.domain.sample.CaptureSample
import com.simprints.core.domain.step.StepResult

@Keep
data class FaceCaptureResult(
    val referenceId: String,
    val results: List<Item>,
) : StepResult {
    @Keep
    data class Item(
        val captureEventId: String?,
        val index: Int,
        val sample: CaptureSample?,
    ) : StepResult
}
