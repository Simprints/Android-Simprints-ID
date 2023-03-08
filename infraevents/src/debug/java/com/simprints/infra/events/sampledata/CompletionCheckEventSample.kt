package com.simprints.infra.events.sampledata

import com.simprints.infra.events.event.domain.models.CompletionCheckEvent
import com.simprints.infra.events.event.domain.models.EventLabels
import com.simprints.infra.events.sampledata.SampleDefaults.CREATED_AT

object CompletionCheckEventSample : SampleEvent() {
    override fun getEvent(
        labels: EventLabels,
        isClosed: Boolean
    ): CompletionCheckEvent {
        return CompletionCheckEvent(CREATED_AT, true, labels)
    }
}
