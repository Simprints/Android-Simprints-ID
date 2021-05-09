package com.simprints.id.sampledata

import com.simprints.id.data.db.event.domain.models.Event
import java.util.*

abstract class SampleEvent {

    abstract fun getEvent(
        sessionId: String = UUID.randomUUID().toString(),
        subjectId: String = UUID.randomUUID().toString(),
        isClosed: Boolean = false
    ): Event

}
