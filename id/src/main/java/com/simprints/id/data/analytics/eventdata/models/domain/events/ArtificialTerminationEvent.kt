package com.simprints.id.data.analytics.eventdata.models.domain.events

import androidx.annotation.Keep

@Keep
class ArtificialTerminationEvent(val relativeStartTime: Long,
                                 val reason: Reason) : Event(EventType.ARTIFICIAL_TERMINATION) {

    @Keep
    enum class Reason {
        TIMED_OUT, NEW_SESSION
    }
}
