package com.simprints.id.data.analytics.eventdata.models.domain.events.callback

import androidx.annotation.Keep
import com.simprints.id.data.analytics.eventdata.models.domain.events.Event
import com.simprints.id.data.analytics.eventdata.models.domain.events.EventType

@Keep
class IdentityConfirmationCallbackEvent(
    startTime: Long,
    val identificationOutcome: Boolean
) : Event(EventType.IDENTITY_CONFIRMATION, startTime)
