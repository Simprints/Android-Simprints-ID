package com.simprints.id.data.db.event.remote.events

import androidx.annotation.Keep
import com.simprints.id.data.db.event.domain.events.ArtificialTerminationEvent

@Keep
class ApiArtificialTerminationEvent(val relativeStartTime: Long,
                                    val reason: ApiReason) : ApiEvent(ApiEventType.ARTIFICIAL_TERMINATION) {

    @Keep
    enum class ApiReason {
        TIMED_OUT, NEW_SESSION
    }

    constructor(alertArtificialTerminationEventDomain: ArtificialTerminationEvent):
        this(alertArtificialTerminationEventDomain.relativeStartTime ?: 0, ApiReason.valueOf(alertArtificialTerminationEventDomain.reason.toString()))
}
