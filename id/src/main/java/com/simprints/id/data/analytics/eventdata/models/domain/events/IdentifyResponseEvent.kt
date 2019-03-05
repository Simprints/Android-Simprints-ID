package com.simprints.id.data.analytics.eventdata.models.domain.events

import com.simprints.id.domain.responses.IdentifyResponse

class IdentifyResponseEvent(val relativeStartTime: Long,
                             val identifyResponse: IdentifyResponse) : Event(EventType.IDENTIFY_RESPONSE)

