package com.simprints.face.controllers.core.events.model

class FaceCaptureRetryEvent(
    startTime: Long,
    endTime: Long
) : Event(EventType.FACE_CAPTURE_RETRY, startTime, endTime)
