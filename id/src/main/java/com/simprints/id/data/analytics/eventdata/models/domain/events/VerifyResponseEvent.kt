package com.simprints.id.data.analytics.eventdata.models.domain.events

import com.simprints.id.domain.responses.VerifyResponse

class VerifyResponseEvent(val relativeStartTime: Long,
                          val verifyResponse: VerifyResponse) : Event(EventType.VERIFY_RESPONSE)
