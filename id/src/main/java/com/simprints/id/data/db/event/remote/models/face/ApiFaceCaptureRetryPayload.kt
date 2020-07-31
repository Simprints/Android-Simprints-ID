package com.simprints.id.data.db.event.remote.models.face

import androidx.annotation.Keep
import com.simprints.id.data.db.event.domain.models.face.FaceCaptureRetryEvent.FaceCaptureRetryPayload
import com.simprints.id.data.db.event.remote.models.ApiEventPayload
import com.simprints.id.data.db.event.remote.models.ApiEventPayloadType.FACE_CAPTURE_RETRY

@Keep
class ApiFaceCaptureRetryPayload(override val relativeStartTime: Long, //Not added on API yet
                                 val relativeEndTime: Long,
                                 override val version: Int) : ApiEventPayload(FACE_CAPTURE_RETRY, version, relativeStartTime) {

    constructor(domainPayload: FaceCaptureRetryPayload) : this(
        domainPayload.createdAt,
        domainPayload.endedAt,
        domainPayload.eventVersion)
}
