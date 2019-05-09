package com.simprints.id.data.analytics.eventdata.models.remote.events

import androidx.annotation.Keep
import com.simprints.id.data.analytics.eventdata.models.domain.events.SuspiciousIntentEvent

@Keep
class ApiSuspiciousIntentEvent(val unexpectedExtras: Map<String, Any?>): ApiEvent(ApiEventType.SUSPICIOUS_INTENT) {

    constructor(suspiciousIntentEvent: SuspiciousIntentEvent) :
        this(suspiciousIntentEvent.unexpectedExtras)
}
