package com.simprints.infra.eventsync.event.remote.models

import androidx.annotation.Keep
import com.simprints.infra.config.store.models.TokenKeyType
import com.simprints.infra.events.event.domain.models.GuidSelectionEvent.GuidSelectionPayload

@Keep
internal data class ApiGuidSelectionPayload(
    override val startTime: Long,
    override val version: Int,
    val selectedId: String,
) : ApiEventPayload(ApiEventPayloadType.GuidSelection, version, startTime) {

    constructor(domainPayload: GuidSelectionPayload) :
        this(domainPayload.createdAt,
            domainPayload.eventVersion,
            domainPayload.selectedId)

    override fun getTokenizedFieldJsonPath(tokenKeyType: TokenKeyType): String? =
        null // this payload doesn't have tokenizable fields
}
