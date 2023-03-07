package com.simprints.infra.events.event.domain.models

import androidx.annotation.Keep
import com.simprints.infra.events.event.domain.models.AuthenticationEvent.AuthenticationPayload.Result
import com.simprints.infra.events.event.domain.models.AuthenticationEvent.AuthenticationPayload.UserInfo
import com.simprints.infra.events.event.domain.models.EventType.AUTHENTICATION
import java.util.*

@Keep
data class AuthenticationEvent(
    override val id: String = UUID.randomUUID().toString(),
    override var labels: EventLabels,
    override val payload: AuthenticationPayload,
    override val type: EventType
) : Event() {

    constructor(
        createdAt: Long,
        endTime: Long,
        userInfo: UserInfo,
        result: Result,
        labels: EventLabels = EventLabels()
    ) : this(
        UUID.randomUUID().toString(),
        labels,
        AuthenticationPayload(createdAt, EVENT_VERSION, endTime, userInfo, result),
        AUTHENTICATION
    )


    @Keep
    data class AuthenticationPayload(
        override val createdAt: Long,
        override val eventVersion: Int,
        override var endedAt: Long,
        val userInfo: UserInfo,
        val result: Result,
        override val type: EventType = AUTHENTICATION
    ) : EventPayload() {

        @Keep
        data class UserInfo(val projectId: String, val userId: String)

        enum class Result {
            AUTHENTICATED,
            BAD_CREDENTIALS,
            OFFLINE,
            TECHNICAL_FAILURE,
            INTEGRITY_SERVICE_ERROR,
            INTEGRITY_SERVICE_TEMPORARY_DOWN_ERROR,
            MISSING_OR_OUTDATED_PLAY_STORE_ERROR,
            BACKEND_MAINTENANCE_ERROR,
            UNKNOWN
        }
    }

    companion object {
        const val EVENT_VERSION = 1
    }
}
