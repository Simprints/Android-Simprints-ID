package com.simprints.infra.eventsync.event.remote.models

import androidx.annotation.Keep
import com.simprints.infra.config.store.models.TokenKeyType
import com.simprints.infra.events.event.domain.models.CompletionCheckEvent.CompletionCheckPayload
import com.simprints.infra.events.event.domain.models.EventType
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Keep
@Serializable
internal data class ApiCompletionCheckPayload(
    override val startTime: ApiTimestamp,
    val completed: Boolean,
) : ApiEventPayload() {
    constructor(domainPayload: CompletionCheckPayload) : this(
        domainPayload.createdAt.fromDomainToApi(),
        domainPayload.completed,
    )

    override fun getTokenizedFieldJsonPath(tokenKeyType: TokenKeyType): String? = null // this payload doesn't have tokenizable fields
}
