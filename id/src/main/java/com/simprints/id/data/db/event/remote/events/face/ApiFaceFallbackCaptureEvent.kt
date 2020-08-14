package com.simprints.id.data.db.event.remote.events.face

import androidx.annotation.Keep
import com.simprints.id.data.db.event.domain.models.face.FaceFallbackCaptureEvent
import com.simprints.id.data.db.event.domain.models.face.FaceFallbackCaptureEvent.FaceFallbackCapturePayload
import com.simprints.id.data.db.event.remote.events.ApiEvent
import com.simprints.id.data.db.event.remote.events.ApiEventPayload
import com.simprints.id.data.db.event.remote.events.ApiEventPayloadType.FACE_FALLBACK_CAPTURE
import com.simprints.id.data.db.event.remote.events.fromDomainToApi

@Keep
class ApiFaceFallbackCaptureEvent(
    val domainEvent: FaceFallbackCaptureEvent
) : ApiEvent(
    domainEvent.id,
    domainEvent.labels.fromDomainToApi(),
    domainEvent.payload.fromDomainToApi()) {

    @Keep
    class ApiFaceFallbackCapturePayload(createdAt: Long,
                                        val endedAt: Long,
                                        version: Int) : ApiEventPayload(FACE_FALLBACK_CAPTURE, version, createdAt) {

        constructor(domainPayload: FaceFallbackCapturePayload) : this(
            domainPayload.createdAt,
            domainPayload.endedAt,
            domainPayload.eventVersion)
    }
}
