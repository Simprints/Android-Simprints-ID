package com.simprints.id.data.analytics.eventdata.models.domain.events

import com.simprints.id.data.analytics.eventdata.models.domain.EventType
import com.simprints.id.session.callout.Callout

class CallbackEvent(val relativeStartTime: Long,
                    val result: Callout) : Event(EventType.CALLBACK)
