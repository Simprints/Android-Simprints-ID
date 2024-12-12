package com.simprints.infra.eventsync.event.remote.models

import androidx.annotation.Keep
import com.simprints.infra.config.store.models.TokenKeyType
import com.simprints.infra.events.event.domain.models.GuidSelectionEvent.GuidSelectionPayload

@Keep
internal data class ApiGuidSelectionPayload(
    override val startTime: ApiTimestamp,
    val selectedId: String,
) : ApiEventPayload(startTime) {
    constructor(domainPayload: GuidSelectionPayload) : this(
        domainPayload.createdAt.fromDomainToApi(),
        domainPayload.selectedId,
    )

    override fun getTokenizedFieldJsonPath(tokenKeyType: TokenKeyType): String? = null // this payload doesn't have tokenizable fields
}
