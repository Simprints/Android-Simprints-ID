package com.simprints.id.data.analytics.eventdata.models.domain.events

import com.simprints.id.domain.requests.IdentifyRequest
import com.simprints.id.domain.requests.Request

class IdentifyRequestEvent(val relativeStartTime: Long,
                           val identifyRequest: IdentifyRequest) : Event(EventType.IDENTIFY_REQUEST)
