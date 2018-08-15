package com.simprints.id.data.analytics.events.models

import com.simprints.id.session.callout.Callout

class CallbackEvent(val relativeStartTime: Long,
                    val result: Callout) : Event(EventType.CALLBACK)
