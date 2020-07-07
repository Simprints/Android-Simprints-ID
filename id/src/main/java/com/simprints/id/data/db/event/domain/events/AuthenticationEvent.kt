package com.simprints.id.data.db.event.domain.events

import androidx.annotation.Keep
import com.simprints.id.data.db.event.domain.events.AuthenticationEvent.AuthenticationPayload.Result
import com.simprints.id.data.db.event.domain.events.AuthenticationEvent.AuthenticationPayload.UserInfo
import java.util.*

@Keep
class AuthenticationEvent(
    creationTime: Long,
    endTime: Long,
    userInfo: UserInfo,
    result: Result,
    sessionId: String = UUID.randomUUID().toString()) : Event(
    UUID.randomUUID().toString(),
    DEFAULT_EVENT_VERSION,
    listOf(EventLabel.SessionId(sessionId)),
    AuthenticationPayload(creationTime, DEFAULT_EVENT_VERSION, endTime, userInfo, result)) {


    @Keep
    class AuthenticationPayload(
        creationTime: Long,
        version: Int,
        val endTime: Long,
        val userInfo: UserInfo,
        val result: Result
    ) : EventPayload(EventPayloadType.AUTHENTICATION, version, creationTime) {

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
