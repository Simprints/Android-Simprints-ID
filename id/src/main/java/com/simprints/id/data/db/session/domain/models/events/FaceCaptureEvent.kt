package com.simprints.id.data.db.session.domain.models.events

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

    class Face(
        val yaw: Float,
        var roll: Float,
        val quality: Float,
        val template: String
    )

    enum class Result {
        VALID,
        INVALID,
        OFF_YAW,
        OFF_ROLL,
        TOO_CLOSE,
        TOO_FAR
    }

}

