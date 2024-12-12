package com.simprints.infra.eventsync.event.remote.models

import androidx.annotation.Keep
import com.simprints.infra.config.store.models.TokenKeyType
import com.simprints.infra.events.event.domain.models.IntentParsingEvent.IntentParsingPayload
import com.simprints.infra.events.event.domain.models.IntentParsingEvent.IntentParsingPayload.IntegrationInfo
import com.simprints.infra.events.event.domain.models.IntentParsingEvent.IntentParsingPayload.IntegrationInfo.COMMCARE
import com.simprints.infra.events.event.domain.models.IntentParsingEvent.IntentParsingPayload.IntegrationInfo.ODK
import com.simprints.infra.events.event.domain.models.IntentParsingEvent.IntentParsingPayload.IntegrationInfo.STANDARD
import com.simprints.infra.eventsync.event.remote.models.ApiIntentParsingPayload.ApiIntegrationInfo

@Keep
internal data class ApiIntentParsingPayload(
    override val startTime: ApiTimestamp,
    val integration: ApiIntegrationInfo,
) : ApiEventPayload(startTime) {
    constructor(domainPayload: IntentParsingPayload) : this(
        domainPayload.createdAt.fromDomainToApi(),
        domainPayload.integration.fromDomainToApi(),
    )

    @Keep
    enum class ApiIntegrationInfo {
        ODK,
        STANDARD,
        COMMCARE,
    }

    override fun getTokenizedFieldJsonPath(tokenKeyType: TokenKeyType): String? = null // this payload doesn't have tokenizable fields
}

internal fun IntegrationInfo.fromDomainToApi() = when (this) {
    ODK -> ApiIntegrationInfo.ODK
    STANDARD -> ApiIntegrationInfo.STANDARD
    COMMCARE -> ApiIntegrationInfo.COMMCARE
}
