package com.simprints.id.data.analytics.eventdata.models.domain.events

import androidx.annotation.Keep
import com.simprints.id.data.analytics.eventdata.models.domain.events.callout.Callout
import com.simprints.id.domain.moduleapi.app.requests.AppIntegrationInfo

@Keep
class CalloutEvent(val integration: IntegrationInfo,
                   val relativeStartTime: Long,
                   val callout: Callout) : Event(EventType.CALLOUT) {

    constructor(integrationInfo: AppIntegrationInfo,
                relativeStartTime: Long,
                callout: Callout) :
        this(IntegrationInfo.fromAppIntegrationInfo(integrationInfo),
            relativeStartTime,
            callout)

    enum class IntegrationInfo {
        ODK, STANDARD;

        companion object {
            fun fromAppIntegrationInfo(appIntegrationInfo: AppIntegrationInfo) =
                when (appIntegrationInfo) {
                    AppIntegrationInfo.ODK -> ODK
                    AppIntegrationInfo.STANDARD -> STANDARD
                }
        }
    }
}
