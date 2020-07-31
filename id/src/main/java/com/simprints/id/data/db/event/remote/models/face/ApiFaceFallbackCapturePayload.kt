package com.simprints.id.data.db.event.remote.models.face

import androidx.annotation.Keep
import com.simprints.id.data.db.event.domain.models.face.FaceFallbackCaptureEvent.FaceFallbackCapturePayload
import com.simprints.id.data.db.event.remote.models.ApiEventPayload
import com.simprints.id.data.db.event.remote.models.ApiEventPayloadType.FACE_FALLBACK_CAPTURE


@Keep
class ApiFaceFallbackCapturePayload(override val relativeStartTime: Long, //Not added on API yet
                                    val relativeEndTime: Long,
                                    override val version: Int) : ApiEventPayload(FACE_FALLBACK_CAPTURE, version, relativeStartTime) {

    constructor(domainPayload: FaceFallbackCapturePayload) : this(
        domainPayload.createdAt,
        domainPayload.endedAt,
        domainPayload.eventVersion)
}
