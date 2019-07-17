package com.simprints.id.data.analytics.eventdata.models.remote.events

import androidx.annotation.Keep
import com.simprints.id.data.analytics.eventdata.models.domain.events.IdentityConfirmationEvent

@Keep
class ApiIdentityConfirmationEvent(
    val relativeStartTime: Long,
    val identificationOutcome: Boolean
) : ApiEvent(ApiEventType.IDENTITY_CONFIRMATION) {

    constructor(identityConfirmationEvent: IdentityConfirmationEvent) :
        this(identityConfirmationEvent.relativeStartTime ?: 0,
            identityConfirmationEvent.identificationOutcome)

}
