package com.simprints.id.data.db.event.remote.models

import com.simprints.id.data.db.event.domain.models.IntentParsingEvent.IntentParsingPayload
import com.simprints.id.data.db.event.domain.models.IntentParsingEvent.IntentParsingPayload.IntegrationInfo
import com.simprints.id.data.db.event.domain.models.IntentParsingEvent.IntentParsingPayload.IntegrationInfo.*
import com.simprints.id.data.db.event.remote.models.ApiIntentParsingPayload.ApiIntegrationInfo
import io.realm.internal.Keep

@Keep
data class ApiIntentParsingPayload(override val relativeStartTime: Long,
                                   override val version: Int,
                                   val integration: ApiIntegrationInfo) : ApiEventPayload(ApiEventPayloadType.IntentParsing, version, relativeStartTime) {

    constructor(domainPayload: IntentParsingPayload, baseStartTime: Long) : this(
        domainPayload.createdAt - baseStartTime,
        domainPayload.eventVersion,
        domainPayload.integration.fromDomainToApi())

    @Keep
    enum class ApiIntegrationInfo {
        ODK,
        STANDARD,
        COMMCARE;
    }
}


fun IntegrationInfo.fromDomainToApi() =
    when (this) {
        ODK -> ApiIntegrationInfo.ODK
        STANDARD -> ApiIntegrationInfo.STANDARD
        COMMCARE -> ApiIntegrationInfo.COMMCARE
    }
