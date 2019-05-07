package com.simprints.id.data.analytics.eventdata.models.domain.events

import androidx.annotation.Keep

@Keep
class CallbackEvent(val relativeStartTime: Long, val callback: Callback): Event(EventType.CALLBACK)
