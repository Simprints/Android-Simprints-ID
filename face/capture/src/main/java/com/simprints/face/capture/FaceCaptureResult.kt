package com.simprints.face.capture

import androidx.annotation.Keep
import com.simprints.infra.images.model.SecuredImageRef
import com.simprints.infra.protection.auxiliary.TemplateAuxData
import java.io.Serializable

@Keep
data class FaceCaptureResult(
    val results: List<Item>,
    val auxData: TemplateAuxData?,
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
