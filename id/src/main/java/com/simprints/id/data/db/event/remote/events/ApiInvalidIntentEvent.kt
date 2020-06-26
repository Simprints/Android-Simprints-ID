package com.simprints.id.data.db.event.remote.events

import androidx.annotation.Keep
import com.simprints.id.data.db.event.domain.events.InvalidIntentEvent

@Keep
class ApiInvalidIntentEvent(val relativeStartTime: Long,
                            val action: String,
                            val extras: Map<String, Any?>) : ApiEvent(ApiEventType.INVALID_INTENT) {

    constructor(invalidIntentEvent: InvalidIntentEvent) :
        this(invalidIntentEvent.payload.creationTime,
            invalidIntentEvent.action,
            invalidIntentEvent.extras)
}
