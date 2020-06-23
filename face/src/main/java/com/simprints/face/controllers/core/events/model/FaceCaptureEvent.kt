package com.simprints.face.controllers.core.events.model

import androidx.annotation.Keep
import com.simprints.core.tools.EncodingUtils
import com.simprints.face.models.FaceDetection
import com.simprints.face.detection.Face as DetectionFace

@Keep
class FaceCaptureEvent(
    startTime: Long,
    endTime: Long,
    val attemptNb: Int,
    val qualityThreshold: Float,
    val result: Result,
    val isFallback: Boolean,
    val eventFace: EventFace?
) : Event(EventType.FACE_CAPTURE, startTime, endTime) {

    @Keep
    class EventFace(val yaw: Float, var roll: Float, val quality: Float, val template: String) {
        companion object {
            fun fromFaceDetectionFace(face: DetectionFace?): EventFace? =
                face?.let {
                    EventFace(
                        it.yaw,
                        it.roll,
                        it.quality,
                        EncodingUtils.byteArrayToBase64(it.template)
                    )
                }
        }
    }

    @Keep
    enum class Result {
        VALID,
        INVALID, // either no face or below threshold
        OFF_YAW,
        OFF_ROLL,
        TOO_CLOSE,
        TOO_FAR;

        companion object {
            fun fromFaceDetectionStatus(status: FaceDetection.Status): Result = when (status) {
                FaceDetection.Status.VALID -> VALID
                FaceDetection.Status.VALID_CAPTURING -> VALID
                FaceDetection.Status.NOFACE -> INVALID
                FaceDetection.Status.OFFYAW -> OFF_YAW
                FaceDetection.Status.OFFROLL -> OFF_ROLL
                FaceDetection.Status.TOOCLOSE -> TOO_CLOSE
                FaceDetection.Status.TOOFAR -> TOO_FAR
            }
        }
    }
}
