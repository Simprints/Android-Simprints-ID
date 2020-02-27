package com.simprints.id.data.db.session.remote.events

import androidx.annotation.Keep
import com.simprints.id.data.db.session.domain.models.events.SuspiciousIntentEvent

@Keep
class ApiSuspiciousIntentEvent(val relativeStartTime: Long,
                               val unexpectedExtras: Map<String, Any?>): ApiEvent(ApiEventType.SUSPICIOUS_INTENT) {

    constructor(suspiciousIntentEvent: SuspiciousIntentEvent) :
        this(suspiciousIntentEvent.relativeStartTime ?: 0,
            suspiciousIntentEvent.unexpectedExtras)
}
