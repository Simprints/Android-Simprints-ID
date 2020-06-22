package com.simprints.face.controllers.core.events.model

class FaceFallbackCaptureEvent(
    startTime: Long,
    endTime: Long
) : Event(EventType.FACE_FALLBACK_CAPTURE, startTime, endTime)
