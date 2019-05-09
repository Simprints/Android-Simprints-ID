package com.simprints.id.data.analytics.eventdata.models.remote.events.callout

import com.simprints.id.data.analytics.eventdata.models.domain.events.callout.CalloutIntegrationInfo
import io.realm.internal.Keep

@Keep
enum class ApiCalloutIntegrationInfo {
    ODK, STANDARD;

    companion object {
        fun fromDomainToApi(integrationInfo: CalloutIntegrationInfo) =
            when (integrationInfo) {
                CalloutIntegrationInfo.ODK -> ODK
                CalloutIntegrationInfo.STANDARD -> STANDARD
            }
    }
}
