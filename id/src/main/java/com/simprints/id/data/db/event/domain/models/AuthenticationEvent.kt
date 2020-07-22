package com.simprints.id.data.db.event.domain.models

import androidx.annotation.Keep
import com.simprints.id.data.db.event.domain.models.AuthenticationEvent.AuthenticationPayload.Result
import com.simprints.id.data.db.event.domain.models.AuthenticationEvent.AuthenticationPayload.UserInfo
import com.simprints.id.data.db.event.domain.models.EventLabel.SessionIdLabel
import com.simprints.id.data.db.event.domain.models.EventType.AUTHENTICATION
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
    mutableListOf(SessionIdLabel(sessionId)),
    AuthenticationPayload(createdAt, EVENT_VERSION, endTime, userInfo, result),
    AUTHENTICATION) {


    @Keep
    class AuthenticationPayload(
        createdAt: Long,
        eventVersion: Int,
        endTimeAt: Long,
        val userInfo: UserInfo,
        val result: Result
    ) : EventPayload(AUTHENTICATION, eventVersion, createdAt, endTimeAt) {

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

    companion object {
        const val EVENT_VERSION = DEFAULT_EVENT_VERSION
    }
}
