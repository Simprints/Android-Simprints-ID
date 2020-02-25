package com.simprints.id.data.db.session.domain.events

import androidx.annotation.Keep

@Keep
class ArtificialTerminationEvent(starTime: Long,
                                 val reason: Reason) : Event(EventType.ARTIFICIAL_TERMINATION, starTime) {

    @Keep
    enum class Reason {
        TIMED_OUT, NEW_SESSION
    }
}
