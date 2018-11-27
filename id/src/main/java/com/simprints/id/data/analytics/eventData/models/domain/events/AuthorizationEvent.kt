package com.simprints.id.data.analytics.eventData.models.domain.events

import com.simprints.id.data.analytics.eventData.models.domain.EventType

class AuthorizationEvent(val relativeStartTime: Long,
                         val result: Result,
                         val userInfo: UserInfo?) : Event(EventType.AUTHORIZATION) {

    enum class Result {
        AUTHORIZED, NOT_AUTHORIZED
    }

    class UserInfo(val projectId: String, val userId: String)
}
