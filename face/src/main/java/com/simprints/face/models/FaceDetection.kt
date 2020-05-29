package com.simprints.face.models

import com.simprints.face.data.moduleapi.face.responses.entities.FaceSample
import com.simprints.face.data.moduleapi.face.responses.entities.SecuredImageRef
import com.simprints.face.detection.Face
import com.simprints.uicomponents.models.PreviewFrame
import java.util.*

data class FaceDetection(
    val frame: PreviewFrame,
    val face: Face?,
    val status: Status,
    var securedImageRef: SecuredImageRef? = null,
    val detectionTime: Long = System.currentTimeMillis(),
    var isFallback: Boolean = false
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

    fun toFaceSample(): FaceSample =
        FaceSample(UUID.randomUUID().toString(), face?.template ?: ByteArray(0), securedImageRef)

    fun hasValidStatus(): Boolean = status == Status.VALID || status == Status.VALID_CAPTURING
    fun isAboveQualityThreshold(qualityThreshold: Float): Boolean =
        face?.let { it.quality > qualityThreshold } ?: false
}
