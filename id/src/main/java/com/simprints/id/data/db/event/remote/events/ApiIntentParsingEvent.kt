package com.simprints.id.data.db.event.remote.events

import com.simprints.id.data.db.event.domain.events.IntentParsingEvent
import com.simprints.id.data.db.event.domain.events.IntentParsingEvent.IntentParsingPayload
import com.simprints.id.data.db.event.domain.events.IntentParsingEvent.IntentParsingPayload.IntegrationInfo
import com.simprints.id.data.db.event.domain.events.IntentParsingEvent.IntentParsingPayload.IntegrationInfo.*
import com.simprints.id.data.db.event.remote.events.ApiIntentParsingEvent.ApiIntentParsingPayload.ApiIntegrationInfo
import io.realm.internal.Keep

@androidx.annotation.Keep
class ApiIntentParsingEvent(domainEvent: IntentParsingEvent) :
    ApiEvent(
        domainEvent.id,
        domainEvent.labels.fromDomainToApi(),
        domainEvent.payload.fromDomainToApi()) {


    @Keep
    class ApiIntentParsingPayload(createdAt: Long,
                                  eventVersion: Int,
                                  val integration: ApiIntegrationInfo) : ApiEventPayload(ApiEventPayloadType.INTENT_PARSING, eventVersion, createdAt) {

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
}

fun IntegrationInfo.fromDomainToApi() =
    when (this) {
        ODK -> ApiIntegrationInfo.ODK
        STANDARD -> ApiIntegrationInfo.STANDARD
        COMMCARE -> ApiIntegrationInfo.COMMCARE
    }
