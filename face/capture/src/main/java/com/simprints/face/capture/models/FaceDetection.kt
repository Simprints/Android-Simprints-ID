package com.simprints.face.capture.models

import android.graphics.Bitmap
import com.simprints.core.tools.time.Timestamp
import com.simprints.face.infra.basebiosdk.detection.Face
import com.simprints.infra.images.model.SecuredImageRef
import java.util.UUID

internal data class FaceDetection(
    val bitmap: Bitmap,
    val face: Face?,
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
        NOFACE,
        OFFYAW,
        OFFROLL,
        TOOCLOSE,
        TOOFAR,
    }

    fun hasValidStatus(): Boolean = status == Status.VALID || status == Status.VALID_CAPTURING
}
