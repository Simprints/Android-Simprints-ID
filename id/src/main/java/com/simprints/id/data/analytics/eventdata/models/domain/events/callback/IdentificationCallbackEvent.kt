package com.simprints.id.data.analytics.eventdata.models.domain.events.callback

import androidx.annotation.Keep
import com.simprints.id.data.analytics.eventdata.models.domain.events.Event
import com.simprints.id.data.analytics.eventdata.models.domain.events.EventType

@Keep
class IdentificationCallbackEvent(val relativeStartTime: Long,
                                  val sessionId: String,
                                  val scores: List<CallbackComparisonScore>) : Event(EventType.CALLBACK_IDENTIFICATION)
