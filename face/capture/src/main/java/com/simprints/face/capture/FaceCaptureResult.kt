package com.simprints.face.capture

import androidx.annotation.Keep
import com.simprints.infra.images.model.SecuredImageRef
import java.io.Serializable

@Keep
data class FaceCaptureResult(
    val referenceId: String,
    val results: List<Item>,
) : Serializable {
    @Keep
    data class Item(
        val captureEventId: String?,
        val index: Int,
        val sample: Sample?,
    ) : Serializable

    @Keep
    data class Sample(
        val faceId: String,
        val template: ByteArray,
        val imageRef: SecuredImageRef?,
        val format: String,
    ) : Serializable
}
