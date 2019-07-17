package com.simprints.id.data.analytics.eventdata.models.domain.events

import androidx.annotation.Keep

@Keep
class IdentityConfirmationEvent(
    startTime: Long,
    val identificationOutcome: Boolean) : Event(EventType.IDENTITY_CONFIRMATION, startTime)
