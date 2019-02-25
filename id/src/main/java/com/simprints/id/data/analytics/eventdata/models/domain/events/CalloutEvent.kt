package com.simprints.id.data.analytics.eventData.models.domain.events

import com.simprints.id.data.analytics.eventData.models.domain.EventType
import com.simprints.id.session.callout.Callout

class CalloutEvent(val relativeStartTime: Long,
                   val parameters: Callout) : Event(EventType.CALLOUT)
