package com.simprints.id.data.db.session.domain.models.events

import androidx.annotation.Keep

@Keep
class AuthenticationEvent(
    startTime: Long,
    endTime: Long,
    val userInfo: UserInfo,
    val result: Result
) : Event(EventType.AUTHENTICATION, startTime, endTime) {

    @Keep
    class UserInfo(val projectId: String, val userId: String)

    enum class Result {
        AUTHENTICATED,
        BAD_CREDENTIALS,
        OFFLINE,
        TECHNICAL_FAILURE,
        SAFETYNET_UNAVAILABLE,
        SAFETYNET_INVALID_CLAIM,
        UNKNOWN
    }
}
