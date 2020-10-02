package com.simprints.id.data.db.event.remote.models.face

import androidx.annotation.Keep
import com.simprints.id.data.db.event.domain.models.face.FaceFallbackCaptureEvent.FaceFallbackCapturePayload
import com.simprints.id.data.db.event.remote.models.ApiEventPayload
import com.simprints.id.data.db.event.remote.models.ApiEventPayloadType.FaceFallbackCapture


@Keep
data class ApiFaceFallbackCapturePayload(override val startTime: Long, //Not added on API yet
                                         val endTime: Long,
                                         override val version: Int) : ApiEventPayload(FaceFallbackCapture, version, startTime) {

    constructor(domainPayload: FaceFallbackCapturePayload) : this(
        domainPayload.createdAt,
        domainPayload.endedAt,
        domainPayload.eventVersion)
}
