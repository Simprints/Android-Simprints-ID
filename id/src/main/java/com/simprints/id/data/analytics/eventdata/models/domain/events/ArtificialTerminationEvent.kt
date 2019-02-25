package com.simprints.id.data.analytics.eventData.models.domain.events

import com.simprints.id.data.analytics.eventData.models.domain.EventType

class ArtificialTerminationEvent(val relativeStartTime: Long,
                                 val reason: Reason) : Event(EventType.ARTIFICIAL_TERMINATION) {

    enum class Reason {
        TIMED_OUT, NEW_SESSION
    }
}
