package com.simprints.face.controllers.core.events.model

class FaceCaptureConfirmationEvent(
    startTime: Long,
    endTime: Long,
    var result: Result
) : Event(EventType.FACE_CAPTURE_CONFIRMATION, startTime, endTime) {

    enum class Result {
        CONTINUE, RECAPTURE
    }
}
