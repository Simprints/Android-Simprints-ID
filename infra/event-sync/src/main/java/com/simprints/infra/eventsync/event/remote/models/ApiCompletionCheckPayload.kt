package com.simprints.infra.eventsync.event.remote.models

import androidx.annotation.Keep
import com.simprints.infra.config.store.models.TokenKeyType
import com.simprints.infra.events.event.domain.models.CompletionCheckEvent.CompletionCheckPayload

@Keep
internal data class ApiCompletionCheckPayload(
    override val startTime: Long,
    override val version: Int,
    val completed: Boolean,
) : ApiEventPayload(ApiEventPayloadType.CompletionCheck, version, startTime) {

    constructor(domainPayload: CompletionCheckPayload) :
        this(domainPayload.createdAt, domainPayload.eventVersion, domainPayload.completed)

    override fun getTokenizedFieldJsonPath(tokenKeyType: TokenKeyType): String? =
        null // this payload doesn't have tokenizable fields
}

