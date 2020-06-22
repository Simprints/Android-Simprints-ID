package com.simprints.face.controllers.core.events.model

import androidx.annotation.Keep

@Keep
class FaceCaptureEvent(
    startTime: Long,
    endTime: Long,
    val attemptNb: Int,
    val qualityThreshold: Float,
    val result: Result,
    val isFallback: Boolean,
    val face: Face?
) : Event(EventType.FACE_CAPTURE, startTime, endTime) {

    @Keep
    class Face(val yaw: Float, var roll: Float, val quality: Float, val template: String)

    @Keep
    enum class Result {
        VALID,
        INVALID, // either no face or below threshold
        OFF_YAW,
        OFF_ROLL,
        TOO_CLOSE,
        TOO_FAR
    }
}
