package com.simprints.face.controllers.core.events.model

import com.simprints.eventsystem.event.domain.models.face.FaceCaptureConfirmationEvent as CoreFaceCaptureConfirmationEvent
import com.simprints.eventsystem.event.domain.models.face.FaceCaptureConfirmationEvent.FaceCaptureConfirmationPayload.Result as CoreFaceCaptureConfirmationEventResult

class FaceCaptureConfirmationEvent(
    startTime: Long,
    endTime: Long,
    var result: Result
) : Event(EventType.FACE_CAPTURE_CONFIRMATION, startTime, endTime) {

    fun fromDomainToCore(): CoreFaceCaptureConfirmationEvent =
        CoreFaceCaptureConfirmationEvent(startTime, endTime, result.fromDomainToCore())

    enum class Result {
        CONTINUE, RECAPTURE;

        fun fromDomainToCore(): CoreFaceCaptureConfirmationEventResult =
            when (this) {
                CONTINUE -> CoreFaceCaptureConfirmationEventResult.CONTINUE
                RECAPTURE -> CoreFaceCaptureConfirmationEventResult.RECAPTURE
            }
    }
}
