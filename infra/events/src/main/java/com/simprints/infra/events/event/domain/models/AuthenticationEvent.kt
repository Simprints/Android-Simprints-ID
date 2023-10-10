package com.simprints.infra.events.event.domain.models

import androidx.annotation.Keep
import com.simprints.core.domain.tokenization.TokenizableString
import com.simprints.infra.config.store.models.TokenKeyType
import com.simprints.infra.events.event.domain.models.AuthenticationEvent.AuthenticationPayload.Result
import com.simprints.infra.events.event.domain.models.AuthenticationEvent.AuthenticationPayload.UserInfo
import com.simprints.infra.events.event.domain.models.EventType.AUTHENTICATION
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

    override fun getTokenizedFields(): Map<TokenKeyType, TokenizableString> =
        mapOf(TokenKeyType.AttendantId to payload.userInfo.userId)

    override fun setTokenizedFields(map: Map<TokenKeyType, TokenizableString>) = this.copy(
        payload = payload.copy(
            userInfo = payload.userInfo.copy(
                userId = map[TokenKeyType.AttendantId] ?: payload.userInfo.userId
            )
        )
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
        data class UserInfo(val projectId: String, val userId: TokenizableString)

        @Keep
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
