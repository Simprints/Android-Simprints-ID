package com.simprints.id.data.analytics.eventdata.models.remote.events

import androidx.annotation.Keep
import com.simprints.id.data.analytics.eventdata.models.domain.events.ConfirmationEvent

@Keep
class ApiConfirmationEvent(
    val relativeStartTime: Long,
    val identificationOutcome: Boolean
) : ApiEvent(ApiEventType.IDENTITY_CONFIRMATION) {

    constructor(confirmationEvent: ConfirmationEvent) :
        this(confirmationEvent.relativeStartTime ?: 0,
            confirmationEvent.identificationOutcome)

}
