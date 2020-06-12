package com.simprints.id.data.db.session.domain.models.events

import androidx.annotation.Keep

@Keep
class ArtificialTerminationEvent(
    startTime: Long,
    val reason: Reason
) : Event(EventType.ARTIFICIAL_TERMINATION, startTime) {

    @Keep
    enum class Reason {
        TIMED_OUT, NEW_SESSION
    }
}
