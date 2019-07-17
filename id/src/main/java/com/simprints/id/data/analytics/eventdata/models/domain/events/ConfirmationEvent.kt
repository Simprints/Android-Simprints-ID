package com.simprints.id.data.analytics.eventdata.models.domain.events

import androidx.annotation.Keep

@Keep
class ConfirmationEvent(
    startTime: Long,
    val identificationOutcome: Boolean
) : Event(EventType.CALLBACK_CONFIRMATION, startTime)
