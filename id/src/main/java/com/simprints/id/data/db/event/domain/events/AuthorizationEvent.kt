package com.simprints.id.data.db.event.domain.events

import androidx.annotation.Keep
import com.simprints.id.data.db.event.domain.events.AuthorizationEvent.AuthorizationPayload.Result
import com.simprints.id.data.db.event.domain.events.AuthorizationEvent.AuthorizationPayload.UserInfo
import com.simprints.id.data.db.event.domain.events.EventPayloadType.AUTHORIZATION
import java.util.*

@Keep
class AuthorizationEvent(
    createdAt: Long,
    result: Result,
    userInfo: UserInfo?,
    sessionId: String = UUID.randomUUID().toString() //StopShip: to change in PAS-993
) : Event(
    UUID.randomUUID().toString(),
    DEFAULT_EVENT_VERSION,
    listOf(EventLabel.SessionId(sessionId)),
    AuthorizationPayload(createdAt, DEFAULT_EVENT_VERSION, result, userInfo)) {

    @Keep
    class AuthorizationPayload(createdAt: Long,
                               eventVersion: Int,
                               val result: Result,
                               val userInfo: UserInfo?) : EventPayload(AUTHORIZATION, eventVersion, createdAt) {

        @Keep
        enum class Result {
            AUTHORIZED, NOT_AUTHORIZED
        }

        @Keep
        class UserInfo(val projectId: String, val userId: String)
    }
}
