package com.simprints.infra.eventsync.event.remote.models

import androidx.annotation.Keep
import com.simprints.infra.events.event.domain.models.IntentParsingEvent.IntentParsingPayload
import com.simprints.infra.events.event.domain.models.IntentParsingEvent.IntentParsingPayload.IntegrationInfo
import com.simprints.infra.events.event.domain.models.IntentParsingEvent.IntentParsingPayload.IntegrationInfo.*
import com.simprints.infra.eventsync.event.remote.models.ApiIntentParsingPayload.ApiIntegrationInfo

@Keep
internal data class ApiIntentParsingPayload(
    override val startTime: Long,
    override val version: Int,
    val integration: ApiIntegrationInfo,
) : ApiEventPayload(ApiEventPayloadType.IntentParsing, version, startTime) {

    constructor(domainPayload: IntentParsingPayload) : this(
        domainPayload.createdAt,
        domainPayload.eventVersion,
        domainPayload.integration.fromDomainToApi())

    @Keep
    enum class ApiIntegrationInfo {
        ODK,
        STANDARD,
        COMMCARE;
    }
}


internal fun IntegrationInfo.fromDomainToApi() =
    when (this) {
        ODK -> ApiIntegrationInfo.ODK
        STANDARD -> ApiIntegrationInfo.STANDARD
        COMMCARE -> ApiIntegrationInfo.COMMCARE
    }
