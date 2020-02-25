package com.simprints.id.data.db.session.domain.events

import androidx.annotation.Keep

@Keep
class InvalidIntentEvent(starTime: Long,
                         val action: String,
                         val extras: Map<String, Any?>): Event(EventType.INVALID_INTENT, starTime)
