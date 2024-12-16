package com.simprints.infra.eventsync.event.remote.models.face

import androidx.annotation.Keep
import com.simprints.infra.config.store.models.TokenKeyType
import com.simprints.infra.events.event.domain.models.face.FaceOnboardingCompleteEvent.FaceOnboardingCompletePayload
import com.simprints.infra.eventsync.event.remote.models.ApiEventPayload
import com.simprints.infra.eventsync.event.remote.models.ApiTimestamp
import com.simprints.infra.eventsync.event.remote.models.fromDomainToApi

@Keep
internal data class ApiFaceOnboardingCompletePayload(
    override val startTime: ApiTimestamp, // Not added on API yet
    val endTime: ApiTimestamp?,
) : ApiEventPayload(startTime) {
    constructor(domainPayload: FaceOnboardingCompletePayload) : this(
        domainPayload.createdAt.fromDomainToApi(),
        domainPayload.endedAt?.fromDomainToApi(),
    )

    override fun getTokenizedFieldJsonPath(tokenKeyType: TokenKeyType): String? = null // this payload doesn't have tokenizable fields
}
