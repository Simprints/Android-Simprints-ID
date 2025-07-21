package com.simprints.fingerprint.capture

import androidx.annotation.Keep
import com.simprints.core.domain.fingerprint.IFingerIdentifier
import com.simprints.core.domain.step.StepResult
import com.simprints.infra.images.model.SecuredImageRef

@Keep
data class FingerprintCaptureResult(
    val referenceId: String,
    var results: List<Item>,
) : StepResult {
    @Keep
    data class Item(
        val captureEventId: String?,
        val identifier: IFingerIdentifier,
        val sample: Sample?,
    ) : StepResult

    @Keep
    data class Sample(
        val fingerIdentifier: IFingerIdentifier,
        val template: ByteArray,
        val templateQualityScore: Int,
        val imageRef: SecuredImageRef?,
        val format: String,
    ) : StepResult
}
