package com.simprints.id.sampledata

import com.simprints.id.data.db.event.domain.models.EventLabels
import com.simprints.id.data.db.event.domain.models.IntentParsingEvent
import com.simprints.id.data.db.event.domain.models.IntentParsingEvent.IntentParsingPayload.IntegrationInfo.COMMCARE
import com.simprints.id.sampledata.SampleDefaults.CREATED_AT

object IntentParsingEventSample : SampleEvent() {
    override fun getEvent(
        labels: EventLabels,
        isClosed: Boolean
    ): IntentParsingEvent {
        return IntentParsingEvent(CREATED_AT, COMMCARE, labels)
    }
}
