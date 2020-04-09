package com.simprints.face.controllers.core.events.model

import androidx.annotation.Keep

@Keep
class FaceCaptureEvent(startTime: Long,
                       endTime: Long,
                       val qualityThreshold: Int,
                       val result: Result,
                       val face: Face?) : Event(EventType.FACE_CAPTURE, startTime, endTime) {

    @Keep
    class Face(val quality: Int, val template: String)

    @Keep
    enum class Result {
        GOOD_SCAN,
        BAD_QUALITY,
        SKIPPED,
        FAILURE_TO_ACQUIRE;
    }

    // TODO: coordinate with Platform to create classes there and Cloud to add correct events
}
