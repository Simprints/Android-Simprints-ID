package com.simprints.infra.eventsync.event.remote.models

import androidx.annotation.Keep
import com.simprints.infra.config.store.models.TokenKeyType
import com.simprints.infra.events.event.domain.models.InvalidIntentEvent.InvalidIntentPayload
import kotlinx.serialization.Serializable

@Keep
@Serializable
internal data class ApiInvalidIntentPayload(
    override val startTime: ApiTimestamp,
    val action: String,
    val extras: Map<String, String?>,
) : ApiEventPayload() {
    constructor(domainPayload: InvalidIntentPayload) : this(
        domainPayload.createdAt.fromDomainToApi(),
        domainPayload.action,
        domainPayload.extras,
    )

    override fun getTokenizedFieldJsonPath(tokenKeyType: TokenKeyType): String? = null // this payload doesn't have tokenizable fields
}
