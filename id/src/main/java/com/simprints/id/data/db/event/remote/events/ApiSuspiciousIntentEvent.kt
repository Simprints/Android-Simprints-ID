package com.simprints.id.data.db.event.remote.events

import androidx.annotation.Keep
import com.simprints.id.data.db.event.domain.events.SuspiciousIntentEvent
import com.simprints.id.data.db.event.domain.events.SuspiciousIntentEvent.SuspiciousIntentPayload

@Keep
class ApiSuspiciousIntentEvent(val relativeStartTime: Long,
                               val unexpectedExtras: Map<String, Any?>) : ApiEvent(ApiEventType.SUSPICIOUS_INTENT) {

    constructor(suspiciousIntentEvent: SuspiciousIntentEvent) :
        this((suspiciousIntentEvent.payload as SuspiciousIntentPayload).creationTime ?: 0,
            suspiciousIntentEvent.payload.unexpectedExtras)
}
