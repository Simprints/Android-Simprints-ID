package com.simprints.id.data.analytics.eventdata.models.remote.events

import com.simprints.id.data.analytics.eventdata.models.domain.events.IntentParsingEvent
import com.simprints.id.data.analytics.eventdata.models.remote.events.ApiIntentParsingEvent.ApiIntegrationInfo.Companion.fromDomainToApi
import io.realm.internal.Keep

@Keep
class ApiIntentParsingEvent(val relativeStartTime: Long,
                            val integration: ApiIntegrationInfo) : ApiEvent(ApiEventType.INTENT_PARSING) {

    constructor(event: IntentParsingEvent) : this(
        event.relativeStartTime ?: 0,
        fromDomainToApi(event.integration)
    )

    @Keep
    enum class ApiIntegrationInfo {
        ODK,
        STANDARD,
        COMMCARE;

        companion object {
            fun fromDomainToApi(integration: IntentParsingEvent.IntegrationInfo) =
                when(integration) {
                    IntentParsingEvent.IntegrationInfo.ODK -> ODK
                    IntentParsingEvent.IntegrationInfo.STANDARD -> STANDARD
                    IntentParsingEvent.IntegrationInfo.COMMCARE -> STANDARD //StopShip: Change it when it's added on the server
                }
        }
    }
}
