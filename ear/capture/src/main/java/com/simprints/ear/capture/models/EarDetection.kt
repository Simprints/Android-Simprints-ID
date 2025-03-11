package com.simprints.ear.capture.models

import android.graphics.Bitmap
import com.simprints.core.tools.time.Timestamp
import com.simprints.ear.infra.basebiosdk.detection.Ear
import com.simprints.infra.images.model.SecuredImageRef
import java.util.UUID

internal data class EarDetection(
    val bitmap: Bitmap,
    val ear: Ear?,
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
        NO_EAR,
    }

    fun hasValidStatus(): Boolean = status == Status.VALID || status == Status.VALID_CAPTURING
}
