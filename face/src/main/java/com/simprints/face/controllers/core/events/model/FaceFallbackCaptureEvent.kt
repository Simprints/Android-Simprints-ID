package com.simprints.face.controllers.core.events.model

import com.simprints.infra.events.event.domain.models.face.FaceFallbackCaptureEvent as CoreFaceFallbackCaptureEvent

class FaceFallbackCaptureEvent(
    startTime: Long,
    endTime: Long
) : Event(EventType.FACE_FALLBACK_CAPTURE, startTime, endTime) {
    fun fromDomainToCore(): CoreFaceFallbackCaptureEvent = CoreFaceFallbackCaptureEvent(startTime, endTime)
}
