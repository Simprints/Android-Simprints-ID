package com.simprints.id.data.analytics.eventdata.models.domain.events

import com.simprints.id.domain.moduleapi.app.responses.AppIdentifyResponse

class IdentifyResponseEvent(val relativeStartTime: Long,
                             val identifyResponse: AppIdentifyResponse) : Event(EventType.IDENTIFY_RESPONSE)

