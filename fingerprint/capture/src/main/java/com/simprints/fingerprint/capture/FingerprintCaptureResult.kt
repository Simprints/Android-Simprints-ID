package com.simprints.fingerprint.capture

import androidx.annotation.Keep
import com.simprints.core.domain.fingerprint.IFingerIdentifier
import com.simprints.infra.images.model.SecuredImageRef
import java.io.Serializable

@Keep
data class FingerprintCaptureResult(
    val referenceId: String,
    var results: List<Item>,
) : Serializable {
    @Keep
    data class Item(
        val captureEventId: String?,
        val identifier: IFingerIdentifier,
        val sample: Sample?,
    ) : Serializable

    @Keep
    data class Sample(
        val fingerIdentifier: IFingerIdentifier,
        val template: ByteArray,
        val templateQualityScore: Int,
        val imageRef: SecuredImageRef?,
        val format: String,
    ) : Serializable
}
