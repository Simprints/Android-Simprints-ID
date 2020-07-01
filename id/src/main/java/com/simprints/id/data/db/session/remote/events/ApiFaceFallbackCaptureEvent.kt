package com.simprints.id.data.db.session.remote.events

import androidx.annotation.Keep
import com.simprints.id.data.db.session.domain.models.events.FaceFallbackCaptureEvent

@Keep
class ApiFaceFallbackCaptureEvent(
    val relativeStartTime: Long
) : ApiEvent(ApiEventType.FACE_FALLBACK_CAPTURE) {

    constructor(
        faceFallbackCaptureEvent: FaceFallbackCaptureEvent
    ) : this(faceFallbackCaptureEvent.relativeStartTime ?: 0)

}
