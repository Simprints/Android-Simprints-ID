package com.simprints.id.data.db.session.domain.models.events

import androidx.annotation.Keep

@Keep
class FaceCaptureConfirmationEvent(
    startTime: Long,
    endTime: Long,
    val result: Result
) : Event(EventType.FACE_CAPTURE_CONFIRMATION, startTime, endTime) {

    enum class Result {
        CONTINUE,
        RECAPTURE
    }

}
