package com.simprints.clientapi.controllers.core.eventData.model

import com.simprints.id.data.analytics.eventdata.models.domain.events.IntentParsingEvent.IntegrationInfo as CoreIntegrationInfo

enum class IntegrationInfo {
    ODK,
    STANDARD,
    COMMCARE
}

fun IntegrationInfo.fromDomainToCore() =
    when(this) {
        IntegrationInfo.ODK -> CoreIntegrationInfo.ODK
        IntegrationInfo.COMMCARE -> CoreIntegrationInfo.COMMCARE
        IntegrationInfo.STANDARD -> CoreIntegrationInfo.STANDARD
    }


