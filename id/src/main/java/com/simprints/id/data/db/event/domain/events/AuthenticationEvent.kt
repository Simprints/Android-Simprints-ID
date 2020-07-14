package com.simprints.id.data.db.event.domain.events

import androidx.annotation.Keep
import com.simprints.id.data.db.event.domain.events.AuthenticationEvent.AuthenticationPayload.Result
import com.simprints.id.data.db.event.domain.events.AuthenticationEvent.AuthenticationPayload.UserInfo
import java.util.*

@Keep
class AuthenticationEvent(
    createdAt: Long,
    endTime: Long,
    userInfo: UserInfo,
    result: Result,
    sessionId: String = UUID.randomUUID().toString()
) : Event(
    UUID.randomUUID().toString(),
    mutableListOf(EventLabel.SessionId(sessionId)),
    AuthenticationPayload(createdAt, DEFAULT_EVENT_VERSION, endTime, userInfo, result)) {


    @Keep
    class AuthenticationPayload(
        createdAt: Long,
        eventVersion: Int,
        val endTime: Long,
        val userInfo: UserInfo,
        val result: Result
    ) : EventPayload(EventPayloadType.AUTHENTICATION, eventVersion, createdAt) {

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
}
