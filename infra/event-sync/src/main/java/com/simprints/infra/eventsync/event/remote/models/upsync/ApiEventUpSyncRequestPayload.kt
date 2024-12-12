package com.simprints.infra.eventsync.event.remote.models.upsync

import androidx.annotation.Keep
import com.simprints.infra.config.store.models.TokenKeyType
import com.simprints.infra.events.event.domain.models.upsync.EventUpSyncRequestEvent
import com.simprints.infra.eventsync.event.remote.models.ApiEventPayload
import com.simprints.infra.eventsync.event.remote.models.ApiTimestamp
import com.simprints.infra.eventsync.event.remote.models.fromDomainToApi

@Keep
internal data class ApiEventUpSyncRequestPayload(
    override val startTime: ApiTimestamp,
    val endTime: ApiTimestamp?,
    val requestId: String,
    val content: ApiUpSyncContent,
    val responseStatus: Int?,
    val errorType: String?,
) : ApiEventPayload(startTime) {
    constructor(domainPayload: EventUpSyncRequestEvent.EventUpSyncRequestPayload) : this(
        domainPayload.createdAt.fromDomainToApi(),
        domainPayload.endedAt?.fromDomainToApi(),
        domainPayload.requestId,
        ApiUpSyncContent(
            domainPayload.content.sessionCount,
            domainPayload.content.eventUpSyncCount,
            domainPayload.content.eventDownSyncCount,
        ),
        domainPayload.responseStatus,
        domainPayload.errorType,
    )

    @Keep
    data class ApiUpSyncContent(
        val sessionCount: Int,
        val eventUpSyncCount: Int,
        val eventDownSyncCount: Int,
    )

    override fun getTokenizedFieldJsonPath(tokenKeyType: TokenKeyType): String? = null
}
