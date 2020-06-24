package com.simprints.id.data.db.event.remote.events

import androidx.annotation.Keep
import com.simprints.id.data.db.event.domain.events.SuspiciousIntentEvent

@Keep
class ApiSuspiciousIntentEvent(val relativeStartTime: Long,
                               val unexpectedExtras: Map<String, Any?>): ApiEvent(ApiEventType.SUSPICIOUS_INTENT) {

    constructor(suspiciousIntentEvent: SuspiciousIntentEvent) :
        this(suspiciousIntentEvent.relativeStartTime ?: 0,
            suspiciousIntentEvent.unexpectedExtras)
}
