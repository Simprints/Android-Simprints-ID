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

        @Suppress("ClassName")
        @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "class")
        @JsonSubTypes(
            JsonSubTypes.Type(value = Result.BACKEND_MAINTENANCE_ERROR::class)
        )
        sealed class Result {
            object AUTHENTICATED : Result()
            object BAD_CREDENTIALS : Result()
            object OFFLINE : Result()
            object TECHNICAL_FAILURE : Result()
            data class BACKEND_MAINTENANCE_ERROR(val estimatedOutage: Long? = null) : Result()
            object SAFETYNET_UNAVAILABLE : Result()
            object SAFETYNET_INVALID_CLAIM : Result()
            object UNKNOWN : Result()
        }
    }

    companion object {
        const val EVENT_VERSION = 1
    }
}
