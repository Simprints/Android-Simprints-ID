package com.simprints.id.data.analytics.eventdata.models.domain.events

import androidx.annotation.Keep
import com.simprints.id.domain.moduleapi.app.requests.AppVerifyRequest

@Keep
class VerifyRequestEvent(val relativeStartTime: Long,
                         val verifyRequest: AppVerifyRequest) : Event(EventType.VERIFY_REQUEST)
