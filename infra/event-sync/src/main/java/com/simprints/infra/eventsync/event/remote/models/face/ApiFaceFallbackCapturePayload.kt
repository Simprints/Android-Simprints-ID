package com.simprints.infra.eventsync.event.remote.models.face

import androidx.annotation.Keep
import com.simprints.infra.config.store.models.TokenKeyType
import com.simprints.infra.events.event.domain.models.face.FaceFallbackCaptureEvent.FaceFallbackCapturePayload
import com.simprints.infra.eventsync.event.remote.models.ApiEventPayload
import com.simprints.infra.eventsync.event.remote.models.ApiEventPayloadType.FaceFallbackCapture


@Keep
internal data class ApiFaceFallbackCapturePayload(
    override val startTime: Long, //Not added on API yet
    val endTime: Long,
    override val version: Int,
) : ApiEventPayload(FaceFallbackCapture, version, startTime) {

    constructor(domainPayload: FaceFallbackCapturePayload) : this(
        domainPayload.createdAt,
        domainPayload.endedAt,
        domainPayload.eventVersion)

    override fun getTokenizedFieldJsonPath(tokenKeyType: TokenKeyType): String? =
        null // this payload doesn't have tokenizable fields
}
