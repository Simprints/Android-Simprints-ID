package com.simprints.id.data.analytics.eventdata.models.domain.events.callout

import androidx.annotation.Keep
import com.simprints.id.domain.moduleapi.app.requests.AppIntegrationInfo

@Keep
enum class CalloutIntegrationInfo {
    ODK, STANDARD;

    companion object {
        fun fromAppIntegrationInfo(appIntegrationInfo: AppIntegrationInfo) =
            when (appIntegrationInfo) {
                AppIntegrationInfo.ODK -> ODK
                AppIntegrationInfo.STANDARD -> STANDARD
            }
    }
}
