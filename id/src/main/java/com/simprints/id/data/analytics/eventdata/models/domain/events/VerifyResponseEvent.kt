package com.simprints.id.data.analytics.eventdata.models.domain.events

import com.simprints.id.domain.moduleapi.app.responses.AppVerifyResponse

class VerifyResponseEvent(val relativeStartTime: Long,
                          val verifyResponse: AppVerifyResponse) : Event(EventType.VERIFY_RESPONSE)
