package com.simprints.id.data.analytics.eventdata.models.domain.events

import com.simprints.id.domain.moduleapi.app.requests.AppIdentityConfirmationRequest

class IdentifyConfirmationRequestEvent(val relativeStartTime: Long,
                                       val identityConfirmationRequest: AppIdentityConfirmationRequest) : Event(EventType.IDENTIFY_CONFIRMATION_REQUEST)
