package com.simprints.id.sampledata

import com.simprints.eventsystem.event.domain.models.Event
import com.simprints.eventsystem.event.domain.models.EventLabels

abstract class SampleEvent {

    abstract fun getEvent(labels: EventLabels, isClosed: Boolean = false): Event

}
