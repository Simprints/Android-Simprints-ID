package com.simprints.id.data.analytics.eventdata.models.domain.events

import com.simprints.id.domain.requests.Request
import com.simprints.id.domain.requests.VerifyRequest

class VerifyRequestEvent(val relativeStartTime: Long,
                         val verifyRequest: VerifyRequest) : Event(EventType.VERIFY_REQUEST)
