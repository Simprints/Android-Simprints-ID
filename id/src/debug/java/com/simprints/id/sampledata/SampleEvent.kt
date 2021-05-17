package com.simprints.id.sampledata

import com.simprints.id.data.db.event.domain.models.Event
import com.simprints.id.data.db.event.domain.models.EventLabels

abstract class SampleEvent {

    abstract fun getEvent(labels: EventLabels, isClosed: Boolean = false): Event

}
