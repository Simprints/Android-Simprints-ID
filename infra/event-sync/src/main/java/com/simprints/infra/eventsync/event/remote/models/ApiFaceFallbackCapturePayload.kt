package com.simprints.infra.eventsync.event.remote.models

import androidx.annotation.Keep
import com.simprints.infra.config.store.models.TokenKeyType
import com.simprints.infra.events.event.domain.models.face.FaceFallbackCaptureEvent.FaceFallbackCapturePayload
import kotlinx.serialization.Serializable

@Keep
@Serializable
internal data class ApiFaceFallbackCapturePayload(
    override val startTime: ApiTimestamp, // Not added on API yet
    val endTime: ApiTimestamp?,
) : ApiEventPayload() {
    constructor(domainPayload: FaceFallbackCapturePayload) : this(
        domainPayload.createdAt.fromDomainToApi(),
        domainPayload.endedAt?.fromDomainToApi(),
    )

    override fun getTokenizedFieldJsonPath(tokenKeyType: TokenKeyType): String? = null // this payload doesn't have tokenizable fields
}
