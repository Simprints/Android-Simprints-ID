package com.simprints.infra.events.sampledata

import com.simprints.infra.events.event.domain.models.Event
import com.simprints.infra.events.event.domain.models.EventLabels

abstract class SampleEvent {

    abstract fun getEvent(labels: EventLabels, isClosed: Boolean = false): Event

}
