package com.simprints.id.data.analytics.eventdata.models.domain.events

import com.simprints.id.domain.moduleapi.app.responses.AppRefusalFormResponse

class RefusalFormResponseEvent(val relativeStartTime: Long,
                               val refusalFormResponseEvent: AppRefusalFormResponse) : Event(EventType.REFUSAL_RESPONSE)

