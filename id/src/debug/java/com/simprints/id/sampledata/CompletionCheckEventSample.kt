package com.simprints.id.sampledata

import com.simprints.eventsystem.event.domain.models.CompletionCheckEvent
import com.simprints.eventsystem.event.domain.models.EventLabels
import com.simprints.id.sampledata.SampleDefaults.CREATED_AT

object CompletionCheckEventSample : SampleEvent() {
    override fun getEvent(
        labels: EventLabels,
        isClosed: Boolean
    ): CompletionCheckEvent {
        return CompletionCheckEvent(CREATED_AT, true, labels)
    }
}
