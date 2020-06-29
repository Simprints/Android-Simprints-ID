package com.simprints.id.data.db.session.domain.models.events

import androidx.annotation.Keep

@Keep
class FaceCaptureRetryEvent(
    startTime: Long,
    endTime: Long
) : Event(EventType.FACE_CAPTURE_RETRY, startTime, endTime)
