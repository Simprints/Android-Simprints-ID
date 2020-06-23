package com.simprints.id.data.db.session.domain.models.events

import androidx.annotation.Keep
import com.simprints.id.data.db.session.domain.models.events.EventPayloadType.AUTHORIZATION
import java.util.*

@Keep
class AuthorizationEvent(
    startTime: Long,
    result: AuthorizationEvent.Result,
    userInfo: AuthorizationEvent.UserInfo?,
    sessionId: String = UUID.randomUUID().toString() //StopShip: to change in PAS-993
) : Event(
    UUID.randomUUID().toString(),
    listOf(EventLabel.SessionId(sessionId)),
    AuthorizationEvent(startTime, result, userInfo)) {

    @Keep
    class AuthorizationEvent(val startTime: Long,
                             val result: Result,
                             val userInfo: UserInfo?) : EventPayload(AUTHORIZATION) {

        @Keep
        enum class Result {
            AUTHORIZED, NOT_AUTHORIZED
        }

        @Keep
        class UserInfo(val projectId: String, val userId: String)
    }
}
