package com.simprints.clientapi.controllers.core.eventData.model

import com.simprints.infra.events.event.domain.models.IntentParsingEvent.IntentParsingPayload.IntegrationInfo as CoreIntegrationInfo


enum class IntegrationInfo {
    ODK,
    STANDARD,
    COMMCARE
}

fun IntegrationInfo.fromDomainToCore() = when (this) {
    IntegrationInfo.ODK -> CoreIntegrationInfo.ODK
    IntegrationInfo.COMMCARE -> CoreIntegrationInfo.COMMCARE
    IntegrationInfo.STANDARD -> CoreIntegrationInfo.STANDARD
}


