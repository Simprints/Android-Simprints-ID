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
    val securedImageRef: SecuredImageRef? = null,
    val detectionTime: Long = System.currentTimeMillis()
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
}
