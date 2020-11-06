package com.simprints.face.controllers.core.events.model

import com.simprints.id.data.db.event.domain.models.face.FaceCaptureRetryEvent as CoreFaceCaptureRetryEvent

class FaceCaptureRetryEvent(
    startTime: Long,
    endTime: Long
) : Event(EventType.FACE_CAPTURE_RETRY, startTime, endTime) {
    fun fromDomainToCore(): CoreFaceCaptureRetryEvent = CoreFaceCaptureRetryEvent(startTime, endTime)
}
