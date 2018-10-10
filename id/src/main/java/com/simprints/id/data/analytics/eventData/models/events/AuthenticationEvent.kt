package com.simprints.id.data.analytics.eventData.models.events

import com.simprints.id.data.analytics.eventData.models.EventType

class AuthenticationEvent(val relativeStartTime: Long,
                          val relativeEndTime: Long,
                          val userInfo: UserInfo,
                          val result: Result) : Event(EventType.AUTHENTICATION) {

    class UserInfo(val projectId: String, val userId: String)

    enum class Result {
        AUTHENTICATED,
        BAD_CREDENTIALS,
        OFFLINE,
        TECHNICAL_FAILURE
    }
}
