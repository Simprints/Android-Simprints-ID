package com.simprints.id.data.db.event.remote.models.face

import androidx.annotation.Keep
import com.simprints.id.data.db.event.domain.models.face.FaceCaptureRetryEvent.FaceCaptureRetryPayload
import com.simprints.id.data.db.event.remote.models.ApiEventPayload
import com.simprints.id.data.db.event.remote.models.ApiEventPayloadType.FACE_CAPTURE_RETRY

@Keep
class ApiFaceCaptureRetryPayload(createdAt: Long,
                                 val endedAt: Long,
                                 version: Int) : ApiEventPayload(FACE_CAPTURE_RETRY, version, createdAt) {

    constructor(domainPayload: FaceCaptureRetryPayload) : this(
        domainPayload.createdAt,
        domainPayload.endedAt,
        domainPayload.eventVersion)
}
