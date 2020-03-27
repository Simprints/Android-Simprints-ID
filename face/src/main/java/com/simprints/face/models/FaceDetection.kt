package com.simprints.face.models

import com.simprints.face.detection.Face
import com.simprints.uicomponents.models.PreviewFrame

data class FaceDetection(
    val frame: PreviewFrame,
    val face: Face?,
    val status: Status,
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

}
