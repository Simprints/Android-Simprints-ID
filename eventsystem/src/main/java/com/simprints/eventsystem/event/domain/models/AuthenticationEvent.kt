package com.simprints.eventsystem.event.domain.models

import androidx.annotation.Keep
import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.simprints.eventsystem.event.domain.models.AuthenticationEvent.AuthenticationPayload.Result
import com.simprints.eventsystem.event.domain.models.AuthenticationEvent.AuthenticationPayload.UserInfo
import com.simprints.eventsystem.event.domain.models.EventType.AUTHENTICATION
import java.util.UUID

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

        @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "class")
        @JsonSubTypes(
            JsonSubTypes.Type(value = Result.BackendMaintenanceError::class)
        )
        sealed class Result {
            object Authenticated : Result()
            object BadCredentials : Result()
            object Offline : Result()
            object TechnicalFailure : Result()
            data class BackendMaintenanceError(val estimatedOutage: Long? = null) : Result()
            object SafetyNetUnavailable : Result()
            object SafetyNetInvalidClaim : Result()
            object Unknown : Result()
        }
    }

    companion object {
        const val EVENT_VERSION = 1
    }
}
