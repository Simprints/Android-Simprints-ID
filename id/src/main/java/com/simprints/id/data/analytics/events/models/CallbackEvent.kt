package com.simprints.id.data.analytics.events.models

import com.simprints.id.session.callout.Callout

class CallbackEvent(var callout: Callout, var relativeStartTime: Long = 0): Event(EventType.CALLBACK)
