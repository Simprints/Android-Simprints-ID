package com.simprints.id.data.analytics.eventdata.models.remote.events

import androidx.annotation.Keep
import com.simprints.id.data.analytics.eventdata.models.domain.events.ArtificialTerminationEvent

@Keep
class ApiArtificialTerminationEvent(val relativeStartTime: Long,
                                    val reason: ApiReason) : ApiEvent(ApiEventType.ARTIFICIAL_TERMINATION) {

    enum class ApiReason {
        TIMED_OUT, NEW_SESSION
    }

    constructor(alertArtificialTerminationEventDomain: ArtificialTerminationEvent):
        this(alertArtificialTerminationEventDomain.relativeStartTime, ApiReason.valueOf(alertArtificialTerminationEventDomain.reason.toString()))
}
