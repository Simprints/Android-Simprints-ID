package com.simprints.id.data.db.session.remote.events

import androidx.annotation.Keep
import com.simprints.id.data.db.session.domain.models.events.ArtificialTerminationEvent

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
