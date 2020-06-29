package com.simprints.id.data.db.session.domain.models.events

import androidx.annotation.Keep

@Keep
class FaceFallbackCaptureEvent(
    startTime: Long,
    endTime: Long
) : Event(EventType.FACE_FALLBACK_CAPTURE, startTime, endTime)
