package com.simprints.id.data.db.session.domain.models.events

import androidx.annotation.Keep

@Keep
class InvalidIntentEvent(startTime: Long,
                         val action: String,
                         val extras: Map<String, Any?>): Event(EventType.INVALID_INTENT, startTime)
