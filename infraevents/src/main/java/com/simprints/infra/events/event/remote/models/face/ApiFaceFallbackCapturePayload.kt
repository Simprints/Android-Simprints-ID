package com.simprints.infra.events.remote.models.face

import androidx.annotation.Keep
import com.simprints.infra.events.event.domain.models.face.FaceFallbackCaptureEvent.FaceFallbackCapturePayload
import com.simprints.infra.events.remote.models.ApiEventPayload
import com.simprints.infra.events.remote.models.ApiEventPayloadType.FaceFallbackCapture


@Keep
data class ApiFaceFallbackCapturePayload(override val startTime: Long, //Not added on API yet
                                         val endTime: Long,
                                         override val version: Int) : ApiEventPayload(FaceFallbackCapture, version, startTime) {

    constructor(domainPayload: FaceFallbackCapturePayload) : this(
        domainPayload.createdAt,
        domainPayload.endedAt,
        domainPayload.eventVersion)
}
