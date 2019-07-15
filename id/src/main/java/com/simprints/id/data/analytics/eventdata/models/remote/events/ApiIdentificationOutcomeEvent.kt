package com.simprints.id.data.analytics.eventdata.models.remote.events

import androidx.annotation.Keep
import com.simprints.id.data.analytics.eventdata.models.domain.events.IdentificationOutcomeEvent

@Keep
class ApiIdentificationOutcomeEvent(
    val relativeStartTime: Long,
    val identificationOutcomeValue: Boolean
) : ApiEvent(ApiEventType.IDENTIFICATION_OUTCOME) {

    constructor(identificationOutcomeEvent: IdentificationOutcomeEvent) :
        this(identificationOutcomeEvent.relativeStartTime ?: 0,
            identificationOutcomeEvent.identificationOutcomeValue)

}
