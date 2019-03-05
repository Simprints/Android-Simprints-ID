package com.simprints.id.data.analytics.eventdata.models.domain.events

import com.simprints.id.domain.responses.RefusalFormResponse

class RefusalFormResponseEvent(val relativeStartTime: Long,
                               val refusalFormResponseEvent: RefusalFormResponse) : Event(EventType.REFUSAL_RESPONSE)

