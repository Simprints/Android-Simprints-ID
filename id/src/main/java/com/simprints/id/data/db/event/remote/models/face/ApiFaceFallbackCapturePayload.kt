package com.simprints.id.data.db.event.remote.models.face

import androidx.annotation.Keep
import com.simprints.id.data.db.event.domain.models.face.FaceFallbackCaptureEvent.FaceFallbackCapturePayload
import com.simprints.id.data.db.event.remote.events.ApiEventPayload
import com.simprints.id.data.db.event.remote.events.ApiEventPayloadType.FACE_FALLBACK_CAPTURE


@Keep
class ApiFaceFallbackCapturePayload(createdAt: Long,
                                    val endedAt: Long,
                                    version: Int) : ApiEventPayload(FACE_FALLBACK_CAPTURE, version, createdAt) {

    constructor(domainPayload: FaceFallbackCapturePayload) : this(
        domainPayload.createdAt,
        domainPayload.endedAt,
        domainPayload.eventVersion)
}
