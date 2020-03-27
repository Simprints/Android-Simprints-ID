package com.simprints.face.models

import com.simprints.core.data.database.models.FaceCapture
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

    fun toFaceCapture(id: String) =
        FaceCapture(
            id,
            "",
            face?.template ?: ByteArray(0),
            face?.quality ?: Float.NEGATIVE_INFINITY
        )
}
