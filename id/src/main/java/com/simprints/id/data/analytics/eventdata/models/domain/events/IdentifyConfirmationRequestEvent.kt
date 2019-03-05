package com.simprints.id.data.analytics.eventdata.models.domain.events

import com.simprints.id.domain.requests.IdentityConfirmationRequest
import com.simprints.id.domain.requests.Request

class IdentifyConfirmationRequestEvent(val relativeStartTime: Long,
                                       val identityConfirmationRequest: IdentityConfirmationRequest) : Event(EventType.IDENTIFY_CONFIRMATION_REQUEST)
