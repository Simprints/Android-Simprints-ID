package com.simprints.id.data.db.event.remote.events

import com.simprints.id.data.db.event.domain.events.IntentParsingEvent
import com.simprints.id.data.db.event.domain.events.IntentParsingEvent.IntentParsingPayload
import com.simprints.id.data.db.event.domain.events.IntentParsingEvent.IntentParsingPayload.IntegrationInfo
import com.simprints.id.data.db.event.remote.events.ApiIntentParsingEvent.ApiIntegrationInfo.Companion.fromDomainToApi
import io.realm.internal.Keep

@Keep
class ApiIntentParsingEvent(val relativeStartTime: Long,
                            val integration: ApiIntegrationInfo) : ApiEvent(ApiEventType.INTENT_PARSING) {

    constructor(event: IntentParsingEvent) : this(
        (event.payload as IntentParsingPayload).creationTime,
        fromDomainToApi(event.payload.integration)
    )

    @Keep
    enum class ApiIntegrationInfo {
        ODK,
        STANDARD,
        COMMCARE;

        companion object {
            fun fromDomainToApi(integration: IntegrationInfo) =
                when(integration) {
                    IntegrationInfo.ODK -> ODK
                    IntegrationInfo.STANDARD -> STANDARD
                    IntegrationInfo.COMMCARE -> COMMCARE
                }
        }
    }
}
