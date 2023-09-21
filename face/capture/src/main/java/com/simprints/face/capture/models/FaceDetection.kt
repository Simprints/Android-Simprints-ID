package com.simprints.face.capture.models

import android.graphics.Bitmap
import com.simprints.infra.facebiosdk.detection.Face
import java.util.UUID

internal data class FaceDetection(
    val bitmap: Bitmap,
    val face: Face?,
    val status: Status,
    var securedImageRef: SecuredImageRef? = null,
    var detectionStartTime: Long = System.currentTimeMillis(),
    var isFallback: Boolean = false,
    var id: String = UUID.randomUUID().toString(),
    var detectionEndTime: Long = System.currentTimeMillis()
) {
    enum class Status {
        VALID,
        VALID_CAPTURING,
        NOFACE,
        OFFYAW,
        OFFROLL,
        TOOCLOSE,
        TOOFAR
    }

    fun hasValidStatus(): Boolean = status == Status.VALID || status == Status.VALID_CAPTURING
    fun isAboveQualityThreshold(qualityThreshold: Int): Boolean =
        face?.let { it.quality > qualityThreshold } ?: false
}
