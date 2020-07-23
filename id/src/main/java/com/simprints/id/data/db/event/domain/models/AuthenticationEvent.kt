package com.simprints.id.data.db.event.domain.models

import androidx.annotation.Keep
import com.simprints.id.data.db.event.domain.models.AuthenticationEvent.AuthenticationPayload.Result
import com.simprints.id.data.db.event.domain.models.AuthenticationEvent.AuthenticationPayload.UserInfo
import com.simprints.id.data.db.event.domain.models.EventLabel.SessionIdLabel
import com.simprints.id.data.db.event.domain.models.EventType.AUTHENTICATION
import java.util.*

@Keep
class AuthenticationEvent(
    override val id: String = UUID.randomUUID().toString(),
    override val labels: MutableList<EventLabel>,
    override val payload: AuthenticationPayload,
    override val type: EventType
) : Event(id, labels, payload, type) {

    constructor(
        createdAt: Long,
        endTime: Long,
        userInfo: UserInfo,
        result: Result,
        sessionId: String = UUID.randomUUID().toString()
    ) : this(
        UUID.randomUUID().toString(),
        mutableListOf<EventLabel>(SessionIdLabel(sessionId)),
        AuthenticationPayload(createdAt, EVENT_VERSION, endTime, userInfo, result),
        AUTHENTICATION)


    @Keep
    class AuthenticationPayload(
        override val createdAt: Long,
        override val eventVersion: Int,
        override val endedAt: Long,
        val userInfo: UserInfo,
        val result: Result
    ) : EventPayload(AUTHENTICATION, eventVersion, createdAt, endedAt) {

        @Keep
        data class UserInfo(val projectId: String, val userId: String)

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
