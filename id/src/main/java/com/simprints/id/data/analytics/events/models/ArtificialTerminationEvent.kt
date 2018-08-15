package com.simprints.id.data.analytics.events.models

class ArtificialTerminationEvent(val relativeStartTime: Long,
                                 val reason: ArtificialTerminationEvent.Reason) : Event(EventType.ARTIFICIAL_TERMINATION) {

    enum class Reason {
        TIMED_OUT, NEW_SESSION
    }
}
