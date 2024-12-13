package com.simprints.infra.eventsync.event.remote.models

import androidx.annotation.Keep
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include
import com.simprints.infra.config.store.models.TokenKeyType
import com.simprints.infra.events.event.domain.models.InvalidIntentEvent.InvalidIntentPayload

@Keep
@JsonInclude(Include.NON_NULL)
internal data class ApiInvalidIntentPayload(
    override val startTime: ApiTimestamp,
    val action: String,
    val extras: Map<String, Any?>,
) : ApiEventPayload(startTime) {
    constructor(domainPayload: InvalidIntentPayload) : this(
        domainPayload.createdAt.fromDomainToApi(),
        domainPayload.action,
        domainPayload.extras,
    )

    override fun getTokenizedFieldJsonPath(tokenKeyType: TokenKeyType): String? = null // this payload doesn't have tokenizable fields
}
