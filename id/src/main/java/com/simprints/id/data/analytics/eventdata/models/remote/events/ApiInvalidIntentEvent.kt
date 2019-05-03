package com.simprints.id.data.analytics.eventdata.models.remote.events

import androidx.annotation.Keep
import com.simprints.id.data.analytics.eventdata.models.domain.events.InvalidIntentEvent

@Keep
class ApiInvalidIntentEvent(val action: String, val extras: HashMap<String, Map<String, Any>?>): ApiEvent(ApiEventType.INVALID_INTENT) {
    constructor(invalidIntentEvent: InvalidIntentEvent):
        this(invalidIntentEvent.action, invalidIntentEvent.extras)
}
