package com.simprints.infra.eventsync.event.remote.models

import androidx.annotation.Keep
import com.simprints.infra.config.store.models.TokenKeyType
import com.simprints.infra.events.event.domain.models.upsync.EventUpSyncRequestEvent
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Keep
@Serializable
internal data class ApiEventUpSyncRequestPayload(
    override val startTime: ApiTimestamp,
    val endTime: ApiTimestamp?,
    val requestId: String,
    val content: ApiUpSyncContent,
    val responseStatus: Int?,
    val errorType: String?,
) : ApiEventPayload() {
    constructor(domainPayload: EventUpSyncRequestEvent.EventUpSyncRequestPayload) : this(
        domainPayload.createdAt.fromDomainToApi(),
        domainPayload.endedAt?.fromDomainToApi(),
        domainPayload.requestId,
        ApiUpSyncContent(
            domainPayload.content.sessionCount,
            domainPayload.content.eventUpSyncCount,
            domainPayload.content.eventDownSyncCount,
            domainPayload.content.sampleUpSyncCount,
        ),
        domainPayload.responseStatus,
        domainPayload.errorType,
    )

    @Keep
    @Serializable
    data class ApiUpSyncContent(
        val sessionCount: Int,
        val eventUpSyncCount: Int,
        val eventDownSyncCount: Int,
        val sampleUpSyncCount: Int,
    )

    override fun getTokenizedFieldJsonPath(tokenKeyType: TokenKeyType): String? = null
}
