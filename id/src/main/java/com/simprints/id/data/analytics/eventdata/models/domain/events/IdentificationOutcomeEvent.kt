package com.simprints.id.data.analytics.eventdata.models.domain.events

import androidx.annotation.Keep

@Keep
class IdentificationOutcomeEvent(
    startTime: Long,
    val identificationOutcomeValue: Boolean) : Event(EventType.IDENTITY_CONFIRMATION, startTime)
