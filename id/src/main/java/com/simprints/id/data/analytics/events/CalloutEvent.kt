package com.simprints.id.data.analytics.events

import com.simprints.id.session.callout.Callout

class CalloutEvent(parseCallout: Callout) : Event(EventType.CALLOUT)
