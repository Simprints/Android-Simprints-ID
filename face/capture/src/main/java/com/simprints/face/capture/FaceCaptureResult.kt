package com.simprints.face.capture

import androidx.annotation.Keep
import com.simprints.core.domain.step.StepResult
import com.simprints.infra.images.model.SecuredImageRef

@Keep
data class FaceCaptureResult(
    val referenceId: String,
    val results: List<Item>,
) : StepResult {
    @Keep
    data class Item(
        val captureEventId: String?,
        val index: Int,
        val sample: Sample?,
    ) : StepResult

    @Keep
    data class Sample(
        val faceId: String,
        val template: ByteArray,
        val imageRef: SecuredImageRef?,
        val format: String,
    ) : StepResult
}
