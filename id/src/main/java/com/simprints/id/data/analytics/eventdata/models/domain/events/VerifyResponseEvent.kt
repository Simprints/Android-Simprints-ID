package com.simprints.id.data.analytics.eventdata.models.domain.events

import androidx.annotation.Keep
import com.simprints.id.domain.moduleapi.app.responses.AppVerifyResponse

@Keep
class VerifyResponseEvent(val relativeStartTime: Long,
                          val verifyResponse: AppVerifyResponse) : Event(EventType.VERIFY_RESPONSE)
