package com.simprints.id.data.analytics.events.models

import com.simprints.id.session.callout.Callout

class CalloutEvent(val relativeStartTime: Long,
                   val parameters: Callout) : Event(EventType.CALLOUT)
