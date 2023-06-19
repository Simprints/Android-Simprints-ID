package com.simprints.infra.events.sampledata

import com.simprints.infra.events.event.domain.models.EventLabels
import com.simprints.infra.events.event.domain.models.IntentParsingEvent
import com.simprints.infra.events.event.domain.models.IntentParsingEvent.IntentParsingPayload.IntegrationInfo.COMMCARE
import com.simprints.infra.events.sampledata.SampleDefaults.CREATED_AT

object IntentParsingEventSample : SampleEvent() {
    override fun getEvent(
        labels: EventLabels,
        isClosed: Boolean
    ): IntentParsingEvent {
        return IntentParsingEvent(CREATED_AT, COMMCARE, labels)
    }
}
