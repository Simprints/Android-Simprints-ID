package com.simprints.id.data.analytics.eventdata.models.domain.events

import androidx.annotation.Keep
import com.simprints.id.data.analytics.eventdata.models.domain.events.callout.Callout

@Keep
class CalloutEvent(val integration: String?,
                   val relativeStartTime: Long,
                   val callout: Callout) : Event(EventType.CALLOUT)
