package com.simprints.id.data.analytics.eventdata.models.domain.events

import androidx.annotation.Keep

@Keep
class AuthenticationEvent(val relativeStartTime: Long,
                          val relativeEndTime: Long,
                          val userInfo: UserInfo,
                          val result: Result) : Event(EventType.AUTHENTICATION) {

    @Keep
    class UserInfo(val projectId: String, val userId: String)

    enum class Result {
        AUTHENTICATED,
        BAD_CREDENTIALS,
        OFFLINE,
        TECHNICAL_FAILURE
    }
}
