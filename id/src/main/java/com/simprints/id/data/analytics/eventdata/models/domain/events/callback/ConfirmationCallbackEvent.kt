package com.simprints.id.data.analytics.eventdata.models.domain.events.callback

import androidx.annotation.Keep
import com.simprints.id.data.analytics.eventdata.models.domain.events.Event
import com.simprints.id.data.analytics.eventdata.models.domain.events.EventType

@Keep
class ConfirmationCallbackEvent(
    startTime: Long,
    val identificationOutcome: Boolean
) : Event(EventType.CALLBACK_CONFIRMATION, startTime)
