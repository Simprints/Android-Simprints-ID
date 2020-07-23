package com.simprints.id.data.db.event.domain.models

import androidx.annotation.Keep
import com.simprints.id.data.db.event.domain.models.AuthorizationEvent.AuthorizationPayload.AuthorizationResult
import com.simprints.id.data.db.event.domain.models.AuthorizationEvent.AuthorizationPayload.UserInfo

import com.simprints.id.data.db.event.domain.models.EventType.AUTHORIZATION
import java.util.*

@Keep
class AuthorizationEvent(
    override val id: String = UUID.randomUUID().toString(),
    override var labels: EventLabels,
    override val payload: AuthorizationPayload,
    override val type: EventType
) : Event(id, labels, payload, type) {

    constructor(
        createdAt: Long,
        result: AuthorizationResult,
        userInfo: UserInfo?,
        labels: EventLabels = EventLabels() //StopShip: to change in PAS-993
    ) : this(
        UUID.randomUUID().toString(),
        labels,
        AuthorizationPayload(createdAt, EVENT_VERSION, result, userInfo),
        AUTHORIZATION)

    @Keep
    class AuthorizationPayload(override val createdAt: Long,
                               override val eventVersion: Int,
                               val result: AuthorizationResult,
                               val userInfo: UserInfo?) : EventPayload(AUTHORIZATION, eventVersion, createdAt) {

        @Keep
        enum class AuthorizationResult {
            AUTHORIZED, NOT_AUTHORIZED
        }

        @Keep
        data class UserInfo(val projectId: String, val userId: String)
    }

    companion object {
        const val EVENT_VERSION = DEFAULT_EVENT_VERSION
    }
}
