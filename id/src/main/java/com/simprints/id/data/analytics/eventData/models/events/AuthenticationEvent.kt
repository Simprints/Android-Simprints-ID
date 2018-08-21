package com.simprints.id.data.analytics.eventData.models.events

import com.simprints.id.data.analytics.eventData.models.EventType

class AuthenticationEvent(val relativeStartTime: Long,
                          val relativeEndTime: Long,
                          val userInfo: LoginInfo,
                          val result: Result) : Event(EventType.AUTHENTICATION) {

    class LoginInfo(val projectId: String, val userId: String)

    enum class Result {
        AUTHENTICATED,
        BAD_CREDENTIALS,
        OFFLINE,
        TECHNICAL_FAILURE
    }
}
