package com.simprints.id.data.db.event.remote.models

import androidx.annotation.Keep
import com.simprints.id.data.db.event.domain.models.SuspiciousIntentEvent.SuspiciousIntentPayload

@Keep
class ApiSuspiciousIntentPayload(createdAt: Long,
                                 eventVersion: Int,
                                 val unexpectedExtras: Map<String, Any?>) : ApiEventPayload(ApiEventPayloadType.SUSPICIOUS_INTENT, eventVersion, createdAt) {

    constructor(domainPayload: SuspiciousIntentPayload) :
        this(domainPayload.createdAt,
            domainPayload.eventVersion,
            domainPayload.unexpectedExtras)
}

