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
    class ApiIntentParsingPayload(val relativeStartTime: Long,
                                  val integration: ApiIntegrationInfo) : ApiEventPayload(ApiEventPayloadType.INTENT_PARSING) {

        constructor(domainPayload: IntentParsingPayload) : this(
            domainPayload.creationTime,
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
