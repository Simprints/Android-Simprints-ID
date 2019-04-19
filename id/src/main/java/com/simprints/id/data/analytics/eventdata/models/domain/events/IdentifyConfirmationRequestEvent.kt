package com.simprints.id.data.analytics.eventdata.models.domain.events

import androidx.annotation.Keep
import com.simprints.id.domain.moduleapi.app.requests.AppIdentityConfirmationRequest

@Keep
class IdentifyConfirmationRequestEvent(val relativeStartTime: Long,
                                       val identityConfirmationRequest: AppIdentityConfirmationRequest) : Event(EventType.IDENTIFY_CONFIRMATION_REQUEST)
