package com.simprints.face.controllers.core.events.model

import com.simprints.id.data.db.session.domain.models.events.FaceFallbackCaptureEvent as CoreFaceFallbackCaptureEvent

class FaceFallbackCaptureEvent(
    startTime: Long,
    endTime: Long
) : Event(EventType.FACE_FALLBACK_CAPTURE, startTime, endTime) {
    fun fromDomainToCore(): CoreFaceFallbackCaptureEvent = CoreFaceFallbackCaptureEvent(startTime, endTime)
}
