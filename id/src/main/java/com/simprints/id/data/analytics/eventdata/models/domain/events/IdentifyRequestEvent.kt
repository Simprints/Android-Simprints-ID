package com.simprints.id.data.analytics.eventdata.models.domain.events

import com.simprints.id.domain.moduleapi.app.requests.AppIdentifyRequest

class IdentifyRequestEvent(val relativeStartTime: Long,
                           val identifyRequest: AppIdentifyRequest) : Event(EventType.IDENTIFY_REQUEST)
