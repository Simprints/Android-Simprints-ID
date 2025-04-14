package com.simprints.document.capture.models

import android.graphics.Bitmap
import com.simprints.core.tools.time.Timestamp
import com.simprints.document.infra.basedocumentsdk.detection.Document
import com.simprints.infra.images.model.SecuredImageRef
import java.util.UUID

internal data class DocumentDetection(
    val bitmap: Bitmap,
    val document: Document?,
    val status: Status,
    var securedImageRef: SecuredImageRef? = null,
    var detectionStartTime: Timestamp,
    var isFallback: Boolean = false,
    var id: String = UUID.randomUUID().toString(),
    var detectionEndTime: Timestamp,
) {
    enum class Status {
        VALID,
        VALID_CAPTURING,
        BAD_QUALITY,
        NODOCUMENT,
        OFFYAW,
        OFFROLL,
        TOOCLOSE,
        TOOFAR,
    }

    fun hasValidStatus(): Boolean = status == Status.VALID || status == Status.VALID_CAPTURING
}
