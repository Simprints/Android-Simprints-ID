package com.simprints.id.data.analytics.eventdata.models.domain.events

import androidx.annotation.Keep

@Keep
class InvalidIntentEvent(val action: String, val extras: Map<String, Any?>): Event(EventType.INVALID_INTENT)
