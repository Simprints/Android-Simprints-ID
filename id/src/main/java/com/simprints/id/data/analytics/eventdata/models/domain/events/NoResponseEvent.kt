package com.simprints.id.data.analytics.eventdata.models.domain.events

import androidx.annotation.Keep

@Keep
class NoResponseEvent(val relativeStartTime: Long) : Event(EventType.NO_RESPONSE)
