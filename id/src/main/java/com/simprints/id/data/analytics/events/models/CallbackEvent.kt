package com.simprints.id.data.analytics.events.models

import com.simprints.id.session.callout.Callout

class CallbackEvent(var relativeStartTime:Long, var result: Callout): Event(EventType.CALLBACK)
