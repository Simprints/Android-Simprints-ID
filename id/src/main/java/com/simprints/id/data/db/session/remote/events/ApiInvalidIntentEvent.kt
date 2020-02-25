package com.simprints.id.data.db.session.remote.events

import androidx.annotation.Keep
import com.simprints.id.data.db.session.domain.events.InvalidIntentEvent

@Keep
class ApiInvalidIntentEvent(val relativeStartTime: Long,
                            val action: String,
                            val extras: Map<String, Any?>) : ApiEvent(ApiEventType.INVALID_INTENT) {

    constructor(invalidIntentEvent: InvalidIntentEvent) :
        this(invalidIntentEvent.relativeStartTime ?: 0,
            invalidIntentEvent.action,
            invalidIntentEvent.extras)
}
