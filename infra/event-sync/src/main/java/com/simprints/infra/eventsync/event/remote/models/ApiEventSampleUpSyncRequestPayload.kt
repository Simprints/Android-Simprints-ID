package com.simprints.infra.eventsync.event.remote.models

import androidx.annotation.Keep
import com.simprints.infra.config.store.models.TokenKeyType
import com.simprints.infra.events.event.domain.models.samples.SampleUpSyncRequestEvent
import kotlinx.serialization.Serializable

@Keep
@Serializable
internal data class ApiEventSampleUpSyncRequestPayload(
    override val startTime: ApiTimestamp,
    val endTime: ApiTimestamp?,
    val requestId: String?,
    val sampleId: String,
    val size: Long,
    val errorType: String?,
) : ApiEventPayload() {
    constructor(domainPayload: SampleUpSyncRequestEvent.SampleUpSyncRequestPayload) : this(
        domainPayload.createdAt.fromDomainToApi(),
        domainPayload.endedAt?.fromDomainToApi(),
        domainPayload.requestId,
        domainPayload.sampleId,
        domainPayload.size,
        domainPayload.errorType,
    )

    override fun getTokenizedFieldJsonPath(tokenKeyType: TokenKeyType): String? = null
}
