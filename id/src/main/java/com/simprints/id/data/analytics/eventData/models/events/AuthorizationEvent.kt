package com.simprints.id.data.analytics.eventData.models.events

import com.simprints.id.data.analytics.eventData.models.EventType

class AuthorizationEvent(val relativeStartTime: Long,
                         val result: Result,
                         val userInfo: Info?) : Event(EventType.AUTHORIZATION) {

    enum class Result {
        AUTHORIZED, NOT_AUTHORIZED
    }

    class Info(val projectId: String, val userId: String)
}
