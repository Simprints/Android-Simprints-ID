package com.simprints.id.data.db.session.remote.events

import androidx.annotation.Keep
import com.simprints.id.data.db.session.domain.models.events.FaceCaptureRetryEvent

@Keep
class ApiFaceCaptureRetryEvent(
    val relativeStartTime: Long
) : ApiEvent(ApiEventType.FACE_CAPTURE_RETRY) {

    constructor(
        faceCaptureRetryEvent: FaceCaptureRetryEvent
    ) : this(faceCaptureRetryEvent.relativeStartTime ?: 0)

}
