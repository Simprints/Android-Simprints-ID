package com.simprints.id.data.db.event.domain.events

import androidx.annotation.Keep
import com.simprints.id.data.db.event.domain.events.AuthenticationEvent.AuthenticationPayload.Result
import com.simprints.id.data.db.event.domain.events.AuthenticationEvent.AuthenticationPayload.UserInfo
import java.util.*

@Keep
class AuthenticationEvent(
    startTime: Long,
    endTime: Long,
    userInfo: UserInfo,
    result: Result,
    sessionId: String = UUID.randomUUID().toString(), //StopShip: to change in PAS-993
    sessionStartTime: Long = 0 //StopShip: to change in PAS-993
) : Event(
    UUID.randomUUID().toString(),
    listOf(EventLabel.SessionId(sessionId)),
    AuthenticationPayload(startTime, startTime - sessionStartTime, endTime, endTime - sessionStartTime, userInfo, result)) {


    @Keep
    class AuthenticationPayload(
        startTime: Long,
        relativeStartTime: Long,
        val endTime: Long,
        val relativeEndTime: Long,
        val userInfo: UserInfo,
        val result: Result
    ) : EventPayload(EventPayloadType.AUTHENTICATION, startTime, relativeStartTime) {

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
